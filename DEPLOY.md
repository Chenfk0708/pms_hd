# 🚀 快速部署指南

本指南介绍如何一键部署 Jeez Fitness 项目到新服务器。

## 📦 项目结构

```
/www/jeez.fitness/
├── nginx/                          # Nginx 配置和 SSL 证书
│   ├── deploy.sh                   # 本地部署脚本
│   ├── *.conf                      # Nginx 配置文件（3个域名）
│   ├── *_nginx/                    # SSL 证书目录（3个域名）
│   ├── docker/                     # Docker 专用配置
│   └── README.md
├── docker/                         # Docker Compose 配置
│   ├── docker-compose.yml
│   ├── .env
│   └── .env.example
├── setup-nginx.sh                  # 配置同步脚本
└── README.md
```

## 🎯 部署方式选择

### 方式一：Docker 一键部署（推荐）

**适用场景**：新服务器，需要完整环境

```bash
# 1. 进入项目目录
cd /www/jeez.fitness/docker

# 2. 复制环境变量模板
cp .env.example .env

# 3. 编辑 .env 文件，配置数据库密码
vim .env

# 4. 一键启动所有服务
docker-compose up -d

# 5. 查看服务状态
docker-compose ps
```

**自动启动的服务**：
- ✅ MySQL 8.0
- ✅ Redis 7
- ✅ RabbitMQ
- ✅ Nacos 配置中心
- ✅ MinIO 对象存储
- ✅ Nginx 反向代理（含 SSL）
- ✅ 所有业务微服务

### 方式二：本地 Nginx 部署

**适用场景**：服务器已有环境，只需更新 Nginx 配置

```bash
# 1. 进入 nginx 目录
cd /www/jeez.fitness/nginx

# 2. 运行部署脚本
./deploy.sh
```

**自动执行的操作**：
- ✅ 检查配置文件
- ✅ 复制 SSL 证书到 `/home/ubuntu/`
- ✅ 部署 Nginx 配置到 `/etc/nginx/`
- ✅ 验证并重载 Nginx

## 🔧 配置同步

如果服务器配置有更新，需要同步到项目：

```bash
cd /www/jeez.fitness

# 运行配置同步脚本
./setup-nginx.sh
```

**同步内容**：
- 从服务器复制最新的 Nginx 配置
- 复制所有 SSL 证书
- 生成 Docker 专用的配置文件

## 🌐 域名配置

部署前确保 DNS 已配置：

```
fitness.dualseason.com       A记录 → 服务器IP
minio.fitness.dualseason.com A记录 → 服务器IP
nacos.fitness.dualseason.com A记录 → 服务器IP
```

## 📋 环境变量配置

编辑 `docker/.env` 文件：

```bash
# MySQL 配置
MYSQL_ROOT_PASSWORD=your_strong_password
MYSQL_DATABASE=jeez_fitness
MYSQL_PORT=3306

# Redis 配置
REDIS_PASSWORD=

# MinIO 配置
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=your_minio_password
MINIO_BUCKET_NAME=jeez-fitness
MINIO_PUBLIC_ENDPOINT=https://minio.fitness.dualseason.com
```

## 🐳 Docker 常用命令

```bash
cd /www/jeez.fitness/docker

# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 查看所有服务状态
docker-compose ps

# 查看实时日志
docker-compose logs -f

# 查看指定服务日志
docker-compose logs -f nginx

# 重启指定服务
docker-compose restart nginx

# 重新构建并启动
docker-compose up -d --build nginx

# 进入容器
docker-compose exec nginx sh
```

## 🔍 验证部署

### 1. 检查服务状态

```bash
docker-compose ps
```

所有服务应该显示 `Up` 状态。

### 2. 测试 HTTPS 访问

```bash
# 测试主站
curl -I https://fitness.dualseason.com

# 测试 MinIO
curl -I https://minio.fitness.dualseason.com

# 测试 Nacos
curl -I https://nacos.fitness.dualseason.com/nacos
```

### 3. 检查 SSL 证书

```bash
# 查看证书信息
echo | openssl s_client -servername fitness.dualseason.com -connect fitness.dualseason.com:443 2>/dev/null | openssl x509 -noout -text
```

## ⚠️ 注意事项

1. **首次部署**：Docker 构建需要较长时间，请耐心等待
2. **数据库初始化**：MySQL 首次启动会导入 `init-sql` 目录的 SQL 文件
3. **Nacos 启动**：Nacos 启动较慢（约 30-60 秒），请等待健康检查通过
4. **DNS 生效**：域名解析可能需要 1-5 分钟才能生效
5. **防火墙**：确保开放 80、443 端口

## 🆘 故障排查

### 1. 502 Bad Gateway

```bash
# 检查后端服务
docker-compose ps

# 查看网关日志
docker-compose logs gateway
```

### 2. SSL 证书错误

```bash
# 检查证书是否存在
docker-compose exec nginx ls -la /etc/nginx/ssl/

# 检查证书是否过期
openssl x509 -in nginx/fitness.dualseason.com_nginx/fitness.dualseason.com_bundle.crt -noout -dates
```

### 3. 服务无法启动

```bash
# 查看详细日志
docker-compose logs --tail=100

# 检查端口占用
sudo netstat -tlnp | grep -E '80|443|3306|6379|8848|9000'
```

## 📞 获取帮助

- 查看详细文档：`nginx/README.md`
- 查看 Docker 文档：`docker/README.md`
- 联系开发团队

---

**部署版本**: v1.0  
**更新日期**: 2025-02-11  
**支持域名**: fitness.dualseason.com, minio.fitness.dualseason.com, nacos.fitness.dualseason.com
