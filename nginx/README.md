# Jeez Fitness Nginx 配置（多域名版本）

本目录包含 Jeez 健身房管理系统的 Nginx 反向代理配置文件和自动化部署脚本。

## 🆕 更新说明

**2025-02-11 更新**：
- ✅ 新增 MinIO 子域名配置：`minio.fitness.dualseason.com`
- ✅ 新增 Nacos 子域名配置：`nacos.fitness.dualseason.com`
- ✅ 支持 Docker 一键部署
- ✅ 支持本地服务器部署

## 📁 目录结构

```
nginx/
├── deploy.sh                          # 本地服务器部署脚本
├── Dockerfile                         # Docker 构建文件
├── nginx.conf                         # Nginx 主配置
├── fitness.dualseason.com.conf        # Fitness 主站配置
├── minio.fitness.dualseason.com.conf  # MinIO 控制台配置（子域名）
├── nacos.fitness.dualseason.com.conf  # Nacos 控制台配置（子域名）
├── fitness.dualseason.com_nginx/      # Fitness SSL 证书
│   ├── fitness.dualseason.com_bundle.crt
│   └── fitness.dualseason.com.key
├── minio.fitness.dualseason.com_nginx/ # MinIO SSL 证书
│   ├── minio.fitness.dualseason.com_bundle.crt
│   └── minio.fitness.dualseason.com.key
├── nacos.fitness.dualseason.com_nginx/ # Nacos SSL 证书
│   ├── nacos.fitness.dualseason.com_bundle.crt
│   └── nacos.fitness.dualseason.com.key
├── docker/                            # Docker 专用配置目录
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── *.conf                         # 适配 Docker 的站点配置
│   └── *_nginx/                       # SSL 证书
└── README.md                          # 本文档
```

## 🌐 访问地址

部署完成后，可以通过以下地址访问：

| 服务 | 地址 | 说明 |
|------|------|------|
| Fitness 主站 | https://fitness.dualseason.com | API 网关入口 |
| MinIO 控制台 | https://minio.fitness.dualseason.com | 对象存储管理 |
| Nacos 控制台 | https://nacos.fitness.dualseason.com/nacos | 配置中心/服务发现 |
| API 文档 | https://fitness.dualseason.com/swagger-gateway.html | Swagger 文档 |

**默认账号密码：**
- MinIO: `minioadmin` / `minioadmin123`
- Nacos: `nacos` / `nacos`

## 🚀 部署方式

### 方式一：本地服务器部署（当前服务器）

适用于已有 SSL 证书，部署到本地 Nginx 服务器。

```bash
cd /www/jeez.fitness/nginx

# 确保配置文件和 SSL 证书已准备好
# 运行部署脚本
./deploy.sh
```

部署脚本会自动：
1. 检查所有配置文件和 SSL 证书
2. 复制 SSL 证书到 `/home/ubuntu/` 目录
3. 部署所有 Nginx 配置到 `/etc/nginx/sites-available/`
4. 验证并重载 Nginx

### 方式二：Docker 一键部署（推荐新服务器）

适用于新服务器，通过 Docker Compose 一键部署完整环境。

```bash
cd /www/jeez.fitness/docker

# 1. 复制环境变量文件
cp .env.example .env

# 2. 编辑 .env 文件，配置数据库密码等
vim .env

# 3. 启动所有服务（包括 Nginx、MySQL、Redis、Nacos、MinIO、应用服务）
docker-compose up -d

# 4. 查看服务状态
docker-compose ps

# 5. 查看 Nginx 日志
docker-compose logs -f nginx
```

## 🔧 配置说明

### 域名配置

需要在 DNS 管理面板添加以下 A 记录：

```
fitness.dualseason.com       → 服务器IP
minio.fitness.dualseason.com → 服务器IP
nacos.fitness.dualseason.com → 服务器IP
```

### SSL 证书路径

**本地部署**：
- `/home/ubuntu/fitness.dualseason.com_nginx/`
- `/home/ubuntu/minio.fitness.dualseason.com_nginx/`
- `/home/ubuntu/nacos.fitness.dualseason.com_nginx/`

**Docker 部署**：
- 容器内 `/etc/nginx/ssl/*_nginx/`
- 证书已打包进镜像

### 端口映射

| 端口 | 协议 | 用途 |
|------|------|------|
| 80 | HTTP | 自动重定向到 HTTPS |
| 443 | HTTPS | 主站及所有子域名 |
| 3306 | MySQL | 数据库（仅本地）|
| 6379 | Redis | 缓存（仅本地）|
| 8848 | Nacos | 配置中心（仅本地）|
| 9000 | MinIO | 对象存储 API（仅本地）|
| 9001 | MinIO | 控制台（仅本地）|

## 🐳 Docker 服务架构

```
                    ┌──────────────────────────────────────┐
                    │         Nginx (80/443)               │
                    │  ┌─────────┬─────────┬─────────────┐ │
                    │  │ fitness │  minio  │    nacos    │ │
                    │  └────┬────┴────┬────┴──────┬──────┘ │
                    └───────┼─────────┼───────────┼────────┘
                            │         │           │
       ┌────────────────────┘         │           └────────────────────┐
       │                              │                                │
┌──────▼──────┐              ┌────────▼──────┐              ┌─────────▼──────┐
│   gateway   │              │     minio     │              │     nacos      │
│   :8080     │              │  :9000/:9001  │              │     :8848      │
└──────┬──────┘              └───────────────┘              └────────────────┘
       │
       ├──────────┬──────────┬──────────┬──────────┬──────────┬──────────┐
       ▼          ▼          ▼          ▼          ▼          ▼          ▼
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
│   auth   │ │  member  │ │   store  │ │equipment │ │   coach  │ │  course  │ │  manager │
│  :8081   │ │  :8082   │ │  :8083   │ │  :8084   │ │  :8085   │ │  :8086   │ │  :8087   │
└──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘
```

## 📝 常用命令

### Docker 部署

```bash
cd /www/jeez.fitness/docker

# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 停止并删除卷（清理数据）
docker-compose down -v

# 重启 Nginx
docker-compose restart nginx

# 查看 Nginx 日志
docker-compose logs -f nginx

# 查看所有服务日志
docker-compose logs -f

# 重新构建 Nginx（修改配置后）
docker-compose up -d --build nginx

# 进入 Nginx 容器
docker-compose exec nginx sh

# 测试 Nginx 配置
docker-compose exec nginx nginx -t
```

### 本地 Nginx 部署

```bash
cd /www/jeez.fitness/nginx

# 部署所有配置
./deploy.sh

# 手动测试配置
sudo nginx -t

# 手动重载配置
sudo systemctl reload nginx

# 查看错误日志
sudo tail -f /var/log/nginx/error.log

# 查看访问日志
sudo tail -f /var/log/nginx/fitness.dualseason.com.access.log
```

### 服务健康检查

```bash
# 检查所有服务状态
docker-compose ps

# 测试 Fitness API
curl https://fitness.dualseason.com/api/actuator/health

# 测试 MinIO
curl https://minio.fitness.dualseason.com/minio/health/live

# 测试 Nacos
curl https://nacos.fitness.dualseason.com/nacos/v1/console/health/readiness
```

## ⚠️ 注意事项

1. **SSL 证书**：确保证书文件格式正确（.crt 和 .key 配对）
2. **DNS 解析**：部署前确保所有域名已正确解析到服务器 IP
3. **端口占用**：确保 80、443 端口未被其他服务占用
4. **防火墙**：确保服务器防火墙放行 80、443 端口
5. **Docker 网络**：所有服务在同一个 `jeez-network` 网络中通信

## 🔒 安全配置

- ✅ HSTS（HTTP Strict Transport Security）
- ✅ X-Frame-Options: DENY
- ✅ X-Content-Type-Options: nosniff
- ✅ X-XSS-Protection
- ✅ 仅允许 TLSv1.2 和 TLSv1.3
- ✅ MinIO 和 Nacos 默认开启认证

## 🆘 故障排查

### 1. SSL 证书错误

```bash
# 检查证书是否完整
openssl x509 -in nginx/fitness.dualseason.com_nginx/fitness.dualseason.com_bundle.crt -text -noout

# 检查证书和私钥是否匹配
openssl x509 -noout -modulus -in nginx/fitness.dualseason.com_nginx/fitness.dualseason.com_bundle.crt | openssl md5
openssl rsa -noout -modulus -in nginx/fitness.dualseason.com_nginx/fitness.dualseason.com.key | openssl md5
```

### 2. 502 Bad Gateway

- 检查后端服务是否正常运行：`docker-compose ps`
- 检查服务日志：`docker-compose logs <service-name>`
- 确认 Docker 网络连通性

### 3. DNS 无法解析

- 检查 DNS 记录：`nslookup fitness.dualseason.com`
- DNS 生效通常需要 1-5 分钟
- 可以使用 `dig` 命令查看 DNS 解析情况

### 4. MinIO 控制台 404

MinIO 控制台路径是 `/nacos`，访问根路径会自动重定向。
- 正确地址：https://nacos.fitness.dualseason.com/nacos

## 📞 技术支持

如有问题，请查看项目文档或联系开发团队。

---

**配置更新日期**: 2025-02-11  
**支持的域名**: fitness.dualseason.com, minio.fitness.dualseason.com, nacos.fitness.dualseason.com
