package com.jeez.common.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.jeez.common.config.MinioProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 图片服务实现类
 * 支持图片上传、删除和临时URL生成
 *
 * @author Jeez.Fitness
 * @since 2025-01-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "image.storage.type", havingValue = "minio")
public class MinIOImageService implements ImageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    // 支持的图片格式
    private static final List<String> ALLOWED_EXTENSIONS = new ArrayList<>();

    static {
        ALLOWED_EXTENSIONS.add("jpeg");
        ALLOWED_EXTENSIONS.add("jpg");
        ALLOWED_EXTENSIONS.add("png");
    }

    // 最大文件大小：60MB
    private static final long MAX_FILE_SIZE = 60L * 1024 * 1024;

    @Override
    public String uploadImage(MultipartFile file, String folderPath) {
        validateFile(file);

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = FileUtil.extName(originalFilename);
            String fileName = System.currentTimeMillis() + "_" + cn.hutool.core.util.IdUtil.randomUUID().substring(0, 8) + "." + extension;
            String objectName = StrUtil.isEmpty(folderPath) ? fileName : folderPath + "/" + fileName;

            try (InputStream stream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName())
                                .object(objectName)
                                .stream(stream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            log.info("文件上传成功：{}", objectName);
            return buildPublicUrl(objectName);
        } catch (MinioException e) {
            log.error("MinIO上传失败", e);
            throw new RuntimeException("图片上传失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("上传过程出错", e);
            throw new RuntimeException("上传过程出错：" + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteImage(String fileUrl) {
        try {
            String objectName = extractObjectName(fileUrl);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                    .bucket(bucketName())
                            .object(objectName)
                            .build()
            );

            log.info("文件删除成功：{}", objectName);
            return true;
        } catch (MinioException e) {
            log.error("MinIO删除失败", e);
            return false;
        } catch (Exception e) {
            log.error("删除过程出错", e);
            return false;
        }
    }

    @Override
    public String getTemporaryUrl(String fileUrl, int expirationMinutes) {
        try {
            String objectName = extractObjectName(fileUrl);

            // MinIO 预签名 URL 有效期最长 7 天
            int maxExpiration = 7 * 24 * 60;
            int expiration = Math.min(expirationMinutes, maxExpiration);

            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName())
                            .object(objectName)
                            .expiry(expiration, TimeUnit.MINUTES)
                            .build()
            );

            log.info("临时URL生成成功：{}", objectName);
            return url;
        } catch (MinioException e) {
            log.error("MinIO生成临时URL失败", e);
            throw new RuntimeException("生成临时URL失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("生成临时URL过程出错", e);
            throw new RuntimeException("生成临时URL过程出错：" + e.getMessage(), e);
        }
    }

    /**
     * 验证文件
     */
    private String bucketName() {
        return minioProperties.getBucketName();
    }

    private String publicEndpoint() {
        return StrUtil.blankToDefault(minioProperties.getPublicEndpoint(), minioProperties.getEndpoint());
    }

    private String buildPublicUrl(String objectName) {
        String base = StrUtil.removeSuffix(publicEndpoint(), "/");
        return base + "/" + bucketName() + "/" + objectName;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过60MB");
        }

        // 验证文件格式
        String fileName = file.getOriginalFilename();
        if (StrUtil.isEmpty(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = FileUtil.extName(fileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件格式，仅支持 JPEG、JPG、PNG");
        }
    }

    /**
     * 从URL中提取对象名称
     */
    private String extractObjectName(String fileUrl) {
        if (StrUtil.isEmpty(fileUrl)) {
            throw new IllegalArgumentException("文件URL不能为空");
        }

        // 如果是完整URL，提取对象名称
        if (fileUrl.startsWith("http")) {
            // 格式: http://endpoint/bucket/objectName
            String[] parts = fileUrl.split(bucketName() + "/");
            if (parts.length > 1) {
                return parts[1];
            }
        }

        // 如果直接是对象名称，直接返回
        return fileUrl;
    }
}
