package com.jeez.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 配置属性（统一供各微服务使用）。
 *
 * <p>支持通过 Spring Boot 的宽松绑定读取：</p>
 * <ul>
 *   <li>YAML: {@code minio.public-endpoint} / {@code minio.publicEndpoint}</li>
 *   <li>ENV: {@code MINIO_PUBLIC_ENDPOINT}</li>
 * </ul>
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * MinIO 服务端地址（需要带协议的完整 URL），例如：http://localhost:9000
     */
    private String endpoint = "http://localhost:9000";

    /**
     * 对外访问地址（用于生成返回给前端/客户端的 URL），默认与 endpoint 一致
     */
    private String publicEndpoint;

    /**
     * 访问密钥（用户名）
     */
    private String accessKey = "minioadmin";

    /**
     * 密钥（密码）
     */
    private String secretKey = "minioadmin123";

    /**
     * 默认存储桶名称
     */
    private String bucketName = "jeez-fitness";

    /**
     * Region 配置（默认为空，由 MinIO 自动判断）
     */
    private String region = "";
}

