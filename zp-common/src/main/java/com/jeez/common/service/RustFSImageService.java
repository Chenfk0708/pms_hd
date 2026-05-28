package com.jeez.common.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * RustFS 图片服务实现类
 * 通过 HTTP 调用 RustFS 服务进行文件上传、删除和临时URL生成
 *
 * @author Jeez.Fitness
 * @since 2025-01-24
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "image.storage.type", havingValue = "rustfs")
public class RustFSImageService implements ImageService {

    @Value("${rustfs.server.url}")
    private String rustfsServerUrl;

    @Value("${rustfs.access-key:}")
    private String accessKey;

    @Value("${rustfs.secret-key:}")
    private String secretKey;

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
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String objectName = StrUtil.isEmpty(folderPath) ? fileName : folderPath + "/" + fileName;

            // 调用 RustFS 上传接口
            HttpResponse response = HttpRequest.post(rustfsServerUrl + "/upload")
                    .header("Authorization", buildAuthHeader())
                    .form("path", objectName)
                    .form("file", file.getInputStream(), fileName)
                    .execute();

            if (response.getStatus() == 200) {
                JSONObject jsonResponse = JSONUtil.parseObj(response.body());

                if (jsonResponse.getBool("success")) {
                    String fileUrl = jsonResponse.getStr("url");
                    log.info("文件上传成功：{}", objectName);
                    return fileUrl;
                } else {
                    String errorMsg = jsonResponse.getStr("message", "未知错误");
                    log.error("RustFS上传失败：{}", errorMsg);
                    throw new RuntimeException("图片上传失败：" + errorMsg);
                }
            } else {
                log.error("RustFS服务返回错误状态码：{}", response.getStatus());
                throw new RuntimeException("图片上传失败，服务返回状态码：" + response.getStatus());
            }
        } catch (Exception e) {
            log.error("上传过程出错", e);
            throw new RuntimeException("上传过程出错：" + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteImage(String fileUrl) {
        try {
            if (StrUtil.isEmpty(fileUrl)) {
                log.warn("文件URL为空");
                return false;
            }

            // 调用 RustFS 删除接口
            HttpResponse response = HttpRequest.post(rustfsServerUrl + "/delete")
                    .header("Authorization", buildAuthHeader())
                    .form("path", fileUrl)
                    .execute();

            if (response.getStatus() == 200) {
                JSONObject jsonResponse = JSONUtil.parseObj(response.body());

                if (jsonResponse.getBool("success")) {
                    log.info("文件删除成功：{}", fileUrl);
                    return true;
                } else {
                    String errorMsg = jsonResponse.getStr("message", "未知错误");
                    log.error("RustFS删除失败：{}", errorMsg);
                    return false;
                }
            } else {
                log.error("RustFS服务返回错误状态码：{}", response.getStatus());
                return false;
            }
        } catch (Exception e) {
            log.error("删除过程出错", e);
            return false;
        }
    }

    @Override
    public String getTemporaryUrl(String fileUrl, int expirationMinutes) {
        try {
            if (StrUtil.isEmpty(fileUrl)) {
                throw new IllegalArgumentException("文件URL不能为空");
            }

            // 调用 RustFS 生成临时URL接口
            HttpResponse response = HttpRequest.post(rustfsServerUrl + "/presigned-url")
                    .header("Authorization", buildAuthHeader())
                    .form("path", fileUrl)
                    .form("expiration", expirationMinutes * 60)
                    .execute();

            if (response.getStatus() == 200) {
                JSONObject jsonResponse = JSONUtil.parseObj(response.body());

                if (jsonResponse.getBool("success")) {
                    String temporaryUrl = jsonResponse.getStr("url");
                    log.info("临时URL生成成功：{}", fileUrl);
                    return temporaryUrl;
                } else {
                    String errorMsg = jsonResponse.getStr("message", "未知错误");
                    log.error("RustFS生成临时URL失败：{}", errorMsg);
                    throw new RuntimeException("生成临时URL失败：" + errorMsg);
                }
            } else {
                log.error("RustFS服务返回错误状态码：{}", response.getStatus());
                throw new RuntimeException("生成临时URL失败，服务返回状态码：" + response.getStatus());
            }
        } catch (Exception e) {
            log.error("生成临时URL过程出错", e);
            throw new RuntimeException("生成临时URL过程出错：" + e.getMessage(), e);
        }
    }

    /**
     * 验证文件
     */
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
     * 构建认证头
     */
    private String buildAuthHeader() {
        if (StrUtil.isEmpty(accessKey) || StrUtil.isEmpty(secretKey)) {
            return "";
        }

        String credentials = accessKey + ":" + secretKey;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Bearer " + encoded;
    }
}
