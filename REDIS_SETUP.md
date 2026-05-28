# Redis 连接问题解决方案

## 问题描述
应用启动时出现 `java.net.ConnectException: Connection refused` 错误，这是因为应用尝试连接 Redis 服务（localhost:6379），但 Redis 未启动。

## 解决方案

### 方案1：启动 Redis 服务（推荐）

如果你需要使用 Redis 功能（如会话管理、缓存等），请启动 Redis 服务：

#### Windows 系统：
1. 下载 Redis for Windows：https://github.com/microsoftarchive/redis/releases
2. 解压后运行 `redis-server.exe`
3. Redis 将在默认端口 6379 启动

#### Docker 方式（推荐）：
```bash
docker run -d --name redis -p 6379:6379 redis:latest
```

#### 使用项目中的 Docker Compose：
如果项目根目录有 `docker-compose.yml`，可以运行：
```bash
docker-compose up -d redis
```

### 方案2：临时禁用 Redis（开发测试用）

如果当前不需要 Redis 功能，可以临时禁用：

1. 打开 `src/main/resources/application.yml`
2. 找到以下配置：
   ```yaml
   spring:
     autoconfigure:
       exclude:
         - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
         # 如果 Redis 未启动，可以取消下面这行的注释来禁用 Redis 自动配置
         # - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
   ```
3. 取消最后一行的注释：
   ```yaml
   spring:
     autoconfigure:
       exclude:
         - org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration
         - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
   ```

**注意：** 禁用 Redis 后，依赖 Redis 的功能（如 Sa-Token 会话管理）将无法正常工作。

### 方案3：使用内存存储替代 Redis（仅用于开发）

对于 Sa-Token，可以配置使用内存存储而不是 Redis：

在 `application.yml` 中添加：
```yaml
sa-token:
  # 使用内存存储（仅开发环境）
  store-type: memory
```

## 检查其他依赖服务

除了 Redis，应用可能还需要以下服务：

1. **MySQL** - 默认连接 `localhost:13306`
   - 确保 MySQL 已启动
   - 数据库名：`zhupai_platform`
   - 用户名：`root`
   - 密码：`123456`

2. **Nacos** - 如果使用服务注册发现
   - 默认端口：8848

## 验证服务是否启动

### 检查 Redis：
```bash
# Windows PowerShell
Test-NetConnection localhost -Port 6379

# 或使用 telnet
telnet localhost 6379
```

### 检查 MySQL：
```bash
# Windows PowerShell
Test-NetConnection localhost -Port 13306
```

## 快速启动所有依赖服务（使用 Docker）

如果项目包含 Docker Compose 配置：

```bash
# 在项目根目录执行
docker-compose up -d
```

这将启动所有必要的依赖服务（Redis、MySQL、Nacos 等）。

## 常见问题

### Q: 为什么需要 Redis？
A: Redis 在本项目中主要用于：
- Sa-Token 会话管理
- 验证码存储
- 缓存数据
- 分布式锁

### Q: 可以在生产环境禁用 Redis 吗？
A: 不建议。生产环境应该使用 Redis 以确保性能和功能完整性。

### Q: Redis 连接超时怎么办？
A: 检查：
1. Redis 服务是否正常运行
2. 防火墙是否阻止了 6379 端口
3. 配置文件中的 host 和 port 是否正确
4. Redis 是否设置了密码（如果设置了，需要在配置中添加 password）
