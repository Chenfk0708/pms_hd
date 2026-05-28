package com.jeez.common.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传服务接口
 * 定义统一的图片上传接口，支持多种实现（如MinIO、阿里云OSS等）
 *
 * @author Jeez.Fitness
 * @since 2025-01-24
 */
public interface ImageService {

    /**
     * 上传图片文件
     *
     * @param file 图片文件，支持 JPEG、JPG、PNG 格式，大小不超过 60MB
     * @param folderPath 文件夹路径（相对于存储桶），如 "business-license/1" 或 "about-us/1"
     * @return 图片访问URL
     * @throws IllegalArgumentException 当文件格式不支持或大小超过限制时抛出
     * @throws Exception 当上传过程发生错误时抛出
     */
    String uploadImage(MultipartFile file, String folderPath);

    /**
     * 删除图片文件
     *
     * @param fileUrl 文件URL或文件路径
     * @return 是否删除成功
     */
    boolean deleteImage(String fileUrl);

    /**
     * 获取图片临时访问URL
     * 某些存储系统（如私有OSS）可能需要生成临时URL
     *
     * @param fileUrl 文件URL或文件路径
     * @param expirationMinutes 过期时间（分钟）
     * @return 临时访问URL
     */
    String getTemporaryUrl(String fileUrl, int expirationMinutes);
}
