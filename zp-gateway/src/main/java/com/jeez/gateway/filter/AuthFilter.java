package com.jeez.gateway.filter;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpLogic;
import com.jeez.gateway.config.GatewayAuthProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

/**
 * 网关认证过滤器
 *
 * @author Jeez
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final GatewayAuthProperties authProperties;

    @Value("${jeez.gateway.trust.secret:dev-gateway-trust-secret-change-me}")
    private String gatewayTrustSecret;

    public AuthFilter(GatewayAuthProperties authProperties) {
        this.authProperties = authProperties;
    }
    /**
     * 不需要认证的路径（仅供参考，实际排除路径从配置读取）
     */
    private static final String[] AUTH_EXCLUDE_PATTERNS = {
            "/",
            "/index.html",
            "/css/**",
            "/js/**",
            "/images/**",
            "/favicon.ico",
            "/swagger-ui.html",
            "/swagger-gateway.html",
            "/**/swagger-ui/**",
            "/**/v3/api-docs/**",
            "/v3/api-docs-gateway/**",
            "/swagger-resources/**",
            "/**/webjars/**",
            "/**/actuator/**",
            "/actuator/**"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getPath().value();

        // CORS 预检请求直接放行
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        // 检查是否需要认证
        if (isExcludePath(path)) {
            return chain.filter(exchange);
        }

        // 获取 Authorization Token
        String token = request.getHeaders().getFirst("Authorization");
        if (token == null || token.trim().isEmpty()) {
            return handleUnauthorized(response, "请先登录");
        }

        // 移除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // 验证 Token 并获取用户信息
            StpLogic stpLogic = SaManager.getStpLogic("login");
            Object loginIdObj = stpLogic.getLoginIdByToken(token);

            // 校验Token是否有效
            if (loginIdObj == null) {
                return handleUnauthorized(response, "Token无效或已过期，请重新登录");
            }

            String userId = loginIdObj.toString();
            String gatewayTimestamp = String.valueOf(System.currentTimeMillis());
            String gatewayMethod = request.getMethod() != null ? request.getMethod().name() : "GET";
            String gatewayPath = path;
            String gatewaySignature = buildGatewaySignature(userId, gatewayTimestamp, gatewayMethod, gatewayPath);

            // 添加用户信息到请求头，传递给后端服务
            // 保留原始 Authorization 头，供下游服务 @SaCheckRole 等注解使用
            String authHeader = request.getHeaders().getFirst("Authorization");
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-Auth-Verified", "true")
                    .header("X-Gateway-Timestamp", gatewayTimestamp)
                    .header("X-Gateway-Method", gatewayMethod)
                    .header("X-Gateway-Path", gatewayPath)
                    .header("X-Gateway-Signature", gatewaySignature)
                    .header("Authorization", authHeader)  // 转发原始 Token
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (NotLoginException e) {
            return handleUnauthorized(response, e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            String result = String.format(
                    "{\"code\":500,\"message\":\"认证失败\",\"data\":null,\"timestamp\":%d,\"success\":false}",
                    System.currentTimeMillis());
            DataBuffer buffer = response.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
    }

    /**
     * 检查路径是否在排除列表中
     */
    private boolean isExcludePath(String path) {
        for (String pattern : authProperties.getExcludePaths()) {
            if (pathMatcher.match(pattern, path)) {
                log.debug("路径 {} 匹配白名单模式: {}", path, pattern);
                return true;
            }
        }
        return false;
    }

    /**
     * 处理未授权响应
     */
    private Mono<Void> handleUnauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String result = String.format(
                "{\"code\":401,\"message\":\"%s\",\"data\":null,\"timestamp\":%d,\"success\":false}",
                message, System.currentTimeMillis());

        DataBuffer buffer = response.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private String buildGatewaySignature(String userId, String timestamp, String method, String path) {
        if (gatewayTrustSecret == null || gatewayTrustSecret.isBlank()) {
            throw new IllegalStateException("Gateway trust secret is not configured");
        }
        try {
            String payload = userId + "\n" + timestamp + "\n" + method.toUpperCase() + "\n" + path;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(gatewayTrustSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] signBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(signBytes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build gateway signature", e);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
