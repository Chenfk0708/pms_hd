package com.jeez.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 请求日志过滤器
 * 记录所有请求的访问日志
 *
 * @author Jeez
 */
@Slf4j
@Component
public class LogRequestFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "UNKNOWN";
        String hostHeader = exchange.getRequest().getHeaders().getFirst("Host");
        String host = hostHeader != null ? hostHeader : "unknown";

        try {
            // 使用 then 确保无论请求成功还是失败都会记录日志
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                logRequest(timestamp, host, method, path, startTime, exchange);
            }));
        } catch (Exception e) {
            // 即使在过滤器链处理过程中发生异常，也要记录日志
            log.error("请求处理过程中发生异常: {} [{}] {} -> {} {} {} {}ms",
                    e.getMessage(), timestamp, host, method, 500, path, System.currentTimeMillis() - startTime, e);
            throw e;
        }
    }

    /**
     * 记录请求日志
     */
    private void logRequest(String timestamp, String host, String method, String path,
            long startTime, ServerWebExchange exchange) {
        try {
            long duration = System.currentTimeMillis() - startTime;
            var status = exchange.getResponse().getStatusCode();
            int statusCode = status != null ? status.value() : 200;
            log.info("[{}] {} -> {} {} {} {}ms", timestamp, host, method, statusCode, path, duration);
        } catch (Exception e) {
            // 即使记录日志时发生异常，也要确保有基本的日志输出
            log.error("记录请求日志时发生异常: {}", e.getMessage(), e);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
