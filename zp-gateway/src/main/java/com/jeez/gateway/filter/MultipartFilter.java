package com.jeez.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Multipart 文件上传过滤器
 * 确保 multipart/form-data 请求的 Content-Type 头正确传递
 *
 * @author Jeez
 */
@Slf4j
@Component
public class MultipartFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 只处理图片上传路径
        if (path.startsWith("/api/images/")) {
            MediaType contentType = request.getHeaders().getContentType();
            log.debug("图片上传请求: path={}, contentType={}", path, contentType);

            // 确保 Content-Type 头被正确传递
            if (contentType != null && contentType.includes(MediaType.MULTIPART_FORM_DATA)) {
                log.info("处理 multipart 文件上传请求: {}", path);

                // 保留原始的 Content-Type 头（包含 boundary）
                String originalContentType = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
                if (originalContentType != null) {
                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header(HttpHeaders.CONTENT_TYPE, originalContentType)
                            .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                }
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // 在 AuthFilter 之后执行，但在其他过滤器之前
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
