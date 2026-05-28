package com.jeez.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * 静态资源配置类 - 为网关提供静态资源访问支持
 *
 * @author Jeez
 */
@Configuration
public class StaticResourceConfig {

    /**
     * 配置根路径路由 - 返回首页
     */
    @Bean
    public RouterFunction<ServerResponse> indexRouter() {
        return RouterFunctions
            .route(GET("/"), request ->
                ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(BodyInserters.fromResource(new ClassPathResource("static/index.html")))
            )
            .andRoute(GET("/index.html"), request ->
                ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(BodyInserters.fromResource(new ClassPathResource("static/index.html")))
            );
    }

    /**
     * 配置CSS资源路由
     */
    @Bean
    public RouterFunction<ServerResponse> cssRouter() {
        return RouterFunctions
            .route(GET("/css/**"), request -> {
                String path = request.path().substring(1); // 移除前导斜杠
                return ok()
                    .contentType(MediaType.valueOf("text/css"))
                    .body(BodyInserters.fromResource(new ClassPathResource("static/" + path)));
            });
    }

    /**
     * 配置JavaScript资源路由
     */
    @Bean
    public RouterFunction<ServerResponse> jsRouter() {
        return RouterFunctions
            .route(GET("/js/**"), request -> {
                String path = request.path().substring(1); // 移除前导斜杠
                return ok()
                    .contentType(MediaType.valueOf("application/javascript"))
                    .body(BodyInserters.fromResource(new ClassPathResource("static/" + path)));
            });
    }

    /**
     * 配置图片资源路由
     */
    @Bean
    public RouterFunction<ServerResponse> imageRouter() {
        return RouterFunctions
            .route(GET("/images/**"), request -> {
                String path = request.path().substring(1);
                String fileName = path.substring(path.lastIndexOf("/") + 1);
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

                MediaType mediaType = switch (extension) {
                    case "png" -> MediaType.IMAGE_PNG;
                    case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
                    case "gif" -> MediaType.IMAGE_GIF;
                    case "svg" -> MediaType.valueOf("image/svg+xml");
                    default -> MediaType.APPLICATION_OCTET_STREAM;
                };

                return ok()
                    .contentType(mediaType)
                    .body(BodyInserters.fromResource(new ClassPathResource("static/" + path)));
            });
    }
}
