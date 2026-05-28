package com.jeez.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关鉴权相关配置
 */
@Component
@ConfigurationProperties(prefix = "jeez.gateway.auth")
public class GatewayAuthProperties {

    /**
     * 不需要鉴权的路径（Ant 风格）
     */
    private List<String> excludePaths = new ArrayList<>();

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths != null ? excludePaths : new ArrayList<>();
    }
}

