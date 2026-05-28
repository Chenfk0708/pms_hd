package com.jeez.gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关全局异常处理器
 *
 * @author Jeez
 */
@Slf4j
@Order(-1)
@Component
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (ex instanceof NotFoundException) {
            return handleNotFound(response, ex);
        } else if (ex instanceof ResponseStatusException) {
            return handleResponseStatusException(response, (ResponseStatusException) ex);
        } else {
            return handleInternalServerError(response, ex);
        }
    }

    private Mono<Void> handleNotFound(ServerHttpResponse response, Throwable ex) {
        log.error("网关路由未找到: {}", ex.getMessage());

        response.setStatusCode(HttpStatus.NOT_FOUND);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String result = String.format(
            "{\"code\":404,\"message\":\"服务不存在或路径错误\",\"data\":null,\"timestamp\":%d,\"success\":false}",
            System.currentTimeMillis()
        );

        DataBuffer buffer = response.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> handleResponseStatusException(ServerHttpResponse response, ResponseStatusException ex) {
        log.error("响应状态异常: {}", ex.getReason());

        response.setStatusCode(ex.getStatusCode());
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String result = String.format(
            "{\"code\":%d,\"message\":\"%s\",\"data\":null,\"timestamp\":%d,\"success\":false}",
            ex.getStatusCode().value(), ex.getReason(), System.currentTimeMillis()
        );

        DataBuffer buffer = response.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> handleInternalServerError(ServerHttpResponse response, Throwable ex) {
        log.error("网关内部错误", ex);

        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String result = String.format(
            "{\"code\":500,\"message\":\"网关内部错误\",\"data\":null,\"timestamp\":%d,\"success\":false}",
            System.currentTimeMillis()
        );

        DataBuffer buffer = response.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}