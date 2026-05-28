package com.jeez.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 图片存储配置属性
 * 支持多种存储后端的配置
 *
 * @author Jeez.Fitness
 * @since 2025-01-24
 */
@Data
@Component
@ConfigurationProperties(prefix = "image.storage")
public class ImageStorageProperties {

    /**
     * 存储类型：minio 或 rustfs
     */
    private String type = "minio";

    /**
     * 最大文件大小(字节),默认 60MB
     */
    private long maxFileSize = 60L * 1024 * 1024;

    /**
     * MinIO 配置
     */
    private MinIOProperties minio = new MinIOProperties();

    /**
     * RustFS 配置
     */
    private RustFSProperties rustfs = new RustFSProperties();

    @Data
    public static class MinIOProperties {
        /**
         * MinIO 服务器地址
         */
        private String endpoint = "http://localhost:9000";

        /**
         * 访问密钥
         */
        private String accessKey = "minioadmin";

        /**
         * 密钥
         */
        private String secretKey = "minioadmin";

        /**
         * 桶名称
         */
        private String bucketName = "jeez-fitness";

        /**
         * 是否使用 SSL
         */
        private boolean useSSL = false;
    }

    @Data
    public static class RustFSProperties {
        /**
         * RustFS 服务器地址
         */
        private String serverUrl = "http://localhost:8080";

        /**
         * 访问密钥
         */
        private String accessKey = "";

        /**
         * 密钥
         */
        private String secretKey = "";
    }
}
