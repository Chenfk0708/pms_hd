# Docker Compose 部署指南

本目录包含 Jeez Fitness 项目的 Docker Compose 配置，用于快速部署整个应用及其依赖服务。

## 服务列表

- **MySQL** - 主数据库 (容器端口 3306，宿主映射可通过 `MYSQL_PORT` 配置，默认 3306)
- **Redis** - 缓存服务 (端口 6379)
- **RabbitMQ** - 消息队列 (端口 5672, 管理界面 15672)
- **MinIO** - 对象存储 (端口 9000, 控制台 9001)
- **Nacos** - 配置中心/服务发现 (端口 8848, gRPC 9848/9849)
- **Gateway** - API 网关 (端口 8080)
- **Auth** - 认证服务 (端口 8081)
- **Member** - 会员服务 (端口 8082)
- **Store** - 门店服务 (端口 8083)
- **Equipment** - 器材服务 (端口 8084)
- **Coach** - 教练服务 (端口 8085)
- **Course** - 课程服务 (端口 8086)
- **Manager** - 管理服务 (端口 8087)

## 快速开始

### 1. 准备工作

#### 1.1 确保证书文件就位

```bash
# 将微信支付私钥文件复制到证书目录
cp /path/to/apiclient_key.pem docker/cert/
```

详见 [cert/README.md](cert/README.md)

#### 1.2 环境变量配置（可选）

创建 `.env` 文件自定义配置：

```env
# MySQL
MYSQL_ROOT_PASSWORD=your_password
MYSQL_DATABASE=jeez_fitness
MYSQL_PORT=13306  # 如本机已占用 3306，可改为 13306

# Redis
REDIS_PASSWORD=your_redis_password

# RabbitMQ
RABBITMQ_USER=admin
RABBITMQ_PASSWORD=your_rabbitmq_password

# MinIO
MINIO_ROOT_USER=admin
MINIO_ROOT_PASSWORD=your_minio_password
MINIO_BUCKET_NAME=jeez-fitness
MINIO_PUBLIC_ENDPOINT=http://localhost:9000

# Nacos
NACOS_NAMESPACE=
```

### 2. 启动服务

#### 2.1 仅启动基础设施服务

```bash
cd docker
docker-compose up -d mysql redis rabbitmq minio minio-init nacos
```

#### 2.2 启动所有服务

```bash
cd docker
docker-compose up -d
```

#### 2.3 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f nacos
docker-compose logs -f member
```

### 3. 配置 Nacos

首次启动后，需要在 Nacos 中配置微信支付参数：

1. 访问 Nacos 控制台: http://localhost:8848/nacos
2. 登录（默认用户名/密码：nacos/nacos）
3. 进入 **配置管理 -> 配置列表**
4. 点击 **+** 创建配置

#### 3.1 创建微信支付配置

- **Data ID**: `wechatpay-config.yaml`
- **Group**: `DEFAULT_GROUP`
- **配置格式**: `YAML`
- **配置内容**:

```yaml
wechatpay:
  merchant-id: "你的商户号"
  private-key-path: "/app/apiclient_key.pem"
  merchant-serial-number: "你的商户证书序列号"
  api-v3-key: "你的APIv3密钥"
  app-id: "你的微信AppID"
  notify-url: "https://your-domain.com/member/api/member/cards/pay-notify"
```

> **注意**: Docker 环境中私钥路径为 `/app/apiclient_key.pem`，本地开发环境中使用 `apiclient_key.pem`（classpath 加载）

#### 3.2 创建公共配置（可选）

参考 [../jeez-member-fitness/nacos-config-examples/](../jeez-member-fitness/nacos-config-examples/) 目录下的示例配置。

### 4. 验证服务

访问以下地址验证服务是否正常：

- Nacos 控制台: http://localhost:8848/nacos
- RabbitMQ 管理界面: http://localhost:15672 (guest/guest)
- MinIO 控制台: http://localhost:9001 (minioadmin/minioadmin123)
- MinIO 默认 Bucket: `jeez-fitness`（`minio-init` 自动创建，可通过 `MINIO_BUCKET_NAME` 修改）
- API 网关健康检查: http://localhost:8080/actuator/health

## 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止服务并删除数据卷（慎用！）
docker-compose down -v
```

## 数据持久化

数据通过 Docker Volume 持久化，包括：

- `mysql-data` - MySQL 数据
- `redis-data` - Redis 数据
- `rabbitmq-data` - RabbitMQ 数据
- `minio-data` - MinIO 对象存储
- `nacos-logs` - Nacos 日志

查看卷信息：

```bash
docker volume ls | grep jeez
```

## 日志管理

服务日志存储在 `docker/logs/` 目录下：

```
logs/
├── gateway/
├── auth/
├── member/
├── store/
├── equipment/
├── coach/
├── course/
└── manager/
```

## 常见问题

### 1. Nacos 启动失败

确保 MySQL 已经正常启动并创建了 `nacos_config` 数据库。查看日志：

```bash
docker-compose logs nacos
```

### 2. Member 服务找不到私钥文件

确保 `docker/cert/apiclient_key.pem` 文件存在。

### 3. 端口冲突

如果端口被占用，可以修改 `docker-compose.yml` 中的端口映射，例如：

```yaml
ports:
  - "13306:3306"  # 将 MySQL 映射到 13306
```

### 4. 内存不足

可以调整服务的 JVM 参数或限制容器内存：

```yaml
environment:
  JVM_XMS: 256m
  JVM_XMX: 512m
```

## 安全建议

1. **生产环境请修改所有默认密码**
2. 不要将 `.env` 文件提交到 Git
3. 使用 Nacos 命名空间隔离不同环境的配置
4. 定期备份数据库和配置
5. 配置防火墙规则，限制服务访问

## 更新服务

```bash
# 重新构建并启动服务
docker-compose up -d --build

# 仅重新构建特定服务
docker-compose up -d --build member
```

## 开发建议

- 开发环境可以只启动基础设施（MySQL、Redis、Nacos 等），微服务在 IDEA 中启动
- 使用 `SPRING_PROFILES_ACTIVE=dev` 在本地运行，连接 Docker 中的基础设施
- Nacos 配置可以在本地禁用，使用 `application.yml` 中的配置

```yaml
# bootstrap.yml
spring:
  cloud:
    nacos:
      config:
        enabled: false  # 本地开发禁用 Nacos
```
