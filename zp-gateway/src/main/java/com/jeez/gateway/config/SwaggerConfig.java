package com.jeez.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger 配置类
 *
 * @author Jeez
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Jeez Fitness API Gateway")
                        .description("Jeez Fitness 健身管理系统统一API网关")
                        .version("1.0.0")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .servers(List.of(
                        new Server()
                                .url("https://fitness.dualseason.com")
                                .description("通过网关域名访问"),
                        new Server()
                                .url("http://43.136.113.63:8080")
                                .description("远程访问网关"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("本地访问网关")
                ));
    }
}