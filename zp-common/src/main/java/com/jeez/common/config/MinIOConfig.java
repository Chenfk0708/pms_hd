package com.jeez.common.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

/**
 * MinIO 配置类
 * 用于初始化和配置 MinIO 客户端
 *
 * @author Jeez.Fitness
 * @since 2025-01-24
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "image.storage.type", havingValue = "minio")
public class MinIOConfig {

    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        try {
            disableSSLVerification();

            MinioClient.Builder builder = MinioClient.builder()
                    .endpoint(minioProperties.getEndpoint())
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey());

            if (minioProperties.getRegion() != null && !minioProperties.getRegion().isEmpty()) {
                builder.region(minioProperties.getRegion());
            }

            MinioClient minioClient = builder.build();

            log.info("MinIO 客户端初始化成功：endpoint={}, bucket={}",
                    minioProperties.getEndpoint(), minioProperties.getBucketName());

            // 确保 bucket 存在
            initBucket(minioClient);

            return minioClient;
        } catch (Exception e) {
            log.error("MinIO 客户端初始化失败", e);
            throw new RuntimeException("MinIO 客户端初始化失败：" + e.getMessage(), e);
        }
    }

    /**
     * 禁用SSL证书验证（仅用于开发/测试环境）
     * 生产环境应使用有效的SSL证书
     */
    private void disableSSLVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            log.warn("已禁用SSL证书验证（仅用于开发环境）");
        } catch (Exception e) {
            log.warn("禁用SSL证书验证失败：{}", e.getMessage());
        }
    }

    /**
     * 初始化 bucket，如果不存在则创建并设置为公开读取
     */
    private void initBucket(MinioClient minioClient) {
        try {
            String bucketName = minioProperties.getBucketName();
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                // 创建 bucket
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("创建 MinIO bucket：{}", bucketName);

                // 设置 bucket 策略为公开读取
                String policy = """
                        {
                            "Version": "2012-10-17",
                            "Statement": [
                                {
                                    "Effect": "Allow",
                                    "Principal": {"AWS": ["*"]},
                                    "Action": ["s3:GetObject"],
                                    "Resource": ["arn:aws:s3:::%s/*"]
                                }
                            ]
                        }
                        """.formatted(bucketName);

                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(policy)
                                .build()
                );
                log.info("设置 bucket {} 为公开读取", bucketName);
            } else {
                log.info("MinIO bucket 已存在：{}", bucketName);
            }
        } catch (Exception e) {
            log.warn("初始化 bucket 失败（可能 MinIO 服务未启动）：{}", e.getMessage());
        }
    }
}
