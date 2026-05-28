# Jeez Common Module

公共模块，包含所有服务共享的工具类、配置和服务。

## ImageService - 图片上传服务

`ImageService` 提供基于 MinIO 的图片上传、验证和删除功能。

### 配置方式

图片上传服务默认是**禁用**的。要启用它，需要在具体服务的 `application.yml` 中添加 MinIO 配置：

```yaml
minio:
  # 启用 MinIO（必须设置为 true）
  enabled: true
  # MinIO 服务地址
  endpoint: localhost
  # MinIO 端口号
  port: 9000
  # 访问密钥
  access-key: minioadmin
  # 密钥
  secret-key: minioadmin
  # 默认存储桶名称
  bucket-name: jeez-fitness
  # 是否使用 HTTPS
  secure: false
```

> Note: `minio.enabled/port/secure` is deprecated. Enable MinIO via `image.storage.type=minio` and use the config below.

```yaml
image:
  storage:
    type: minio

minio:
  endpoint: http://localhost:9000
  public-endpoint: http://localhost:9000
  accessKey: minioadmin
  secretKey: minioadmin123
  bucketName: jeez-fitness
```

Bucket will be created automatically (or via Docker Compose `minio-init`).

### 功能特性

- **文件格式验证**：仅支持 jpg/jpeg/png
- **文件大小限制**：单个文件最大 60MB
- **自动命名**：使用时间戳前缀生成文件名
- **条件加载**：`image.storage.type=minio` 时启用

### 使用示例

```java
@RestController
public class MyController {

    @Autowired(required = false)
    private ImageService imageService;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        // 检查服务是否可用
        if (imageService == null) {
            return Result.error("图片上传服务未启用，请配置 MinIO");
        }

        try {
            // 上传图片
            String url = imageService.uploadImage(file, "my-folder");
            return Result.success("上传成功", url);
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
```

### 注意事项

1. **可选依赖**：使用 `@Autowired(required = false)` 注入 ImageService
2. **空值检查**：调用前检查 `imageService != null`
3. **MinIO 配置**：确保 MinIO 服务已启动且配置正确
4. **存储桶创建**：Bucket 会自动创建（或由 Docker Compose `minio-init` 创建）

### MinIO 安装与启动

使用 Docker 快速启动 MinIO：

```bash
docker run -d \
  -p 9000:9000 \
  -p 9001:9001 \
  --name minio \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  minio/minio server /data --console-address ":9001"
```

访问 MinIO 控制台：http://localhost:9001

## 其他工具类

### OkHttpUtil

提供基于 OkHttp 的 HTTP 请求工具方法。

### Result / PageResult

统一的 API 响应结果封装类。
