package com.jeez.zp.crm.config;

import com.jeez.zp.crm.security.GatewayTrustInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final GatewayTrustInterceptor gatewayTrustInterceptor;

    public WebMvcConfig(GatewayTrustInterceptor gatewayTrustInterceptor) {
        this.gatewayTrustInterceptor = gatewayTrustInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(gatewayTrustInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/crm-service/ping", "/actuator/**");
    }
}
