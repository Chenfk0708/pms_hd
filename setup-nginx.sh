#!/bin/bash
# ============================================================================
# Jeez Fitness 项目完整部署脚本
# 功能: 将项目 nginx 配置和 SSL 证书复制到项目目录，支持 Docker 部署
# 用法: ./setup-nginx.sh
# ============================================================================

set -e

# 颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Jeez Fitness Nginx 配置同步脚本${NC}"
echo -e "${BLUE}========================================${NC}"

# 项目目录
PROJECT_DIR="/www/jeez.fitness"
NGINX_DIR="$PROJECT_DIR/nginx"
DOCKER_NGINX_DIR="$NGINX_DIR/docker"

# 确保目录存在
mkdir -p "$DOCKER_NGINX_DIR"

echo -e "\n${YELLOW}[1/4] 复制 SSL 证书...${NC}"

# 复制 Fitness SSL 证书
if [ -d "/home/ubuntu/fitness.dualseason.com_nginx" ]; then
    cp -r /home/ubuntu/fitness.dualseason.com_nginx "$NGINX_DIR/"
    cp -r /home/ubuntu/fitness.dualseason.com_nginx "$DOCKER_NGINX_DIR/"
    echo -e "  ✓ fitness.dualseason.com SSL 证书已复制"
fi

# 复制 MinIO SSL 证书
if [ -d "/home/ubuntu/minio.fitness.dualseason.com_nginx" ]; then
    cp -r /home/ubuntu/minio.fitness.dualseason.com_nginx "$NGINX_DIR/"
    cp -r /home/ubuntu/minio.fitness.dualseason.com_nginx "$DOCKER_NGINX_DIR/"
    echo -e "  ✓ minio.fitness.dualseason.com SSL 证书已复制"
fi

# 复制 Nacos SSL 证书
if [ -d "/home/ubuntu/nacos.fitness.dualseason.com_nginx" ]; then
    cp -r /home/ubuntu/nacos.fitness.dualseason.com_nginx "$NGINX_DIR/"
    cp -r /home/ubuntu/nacos.fitness.dualseason.com_nginx "$DOCKER_NGINX_DIR/"
    echo -e "  ✓ nacos.fitness.dualseason.com SSL 证书已复制"
fi

echo -e "\n${YELLOW}[2/4] 复制 Nginx 配置文件...${NC}"

# 复制主配置文件
if [ -f "/etc/nginx/sites-available/fitness.dualseason.com.conf" ]; then
    cp /etc/nginx/sites-available/fitness.dualseason.com.conf "$NGINX_DIR/"
    echo -e "  ✓ fitness.dualseason.com.conf 已复制"
fi

if [ -f "/etc/nginx/sites-available/minio.fitness.dualseason.com.conf" ]; then
    cp /etc/nginx/sites-available/minio.fitness.dualseason.com.conf "$NGINX_DIR/"
    echo -e "  ✓ minio.fitness.dualseason.com.conf 已复制"
fi

if [ -f "/etc/nginx/sites-available/nacos.fitness.dualseason.com.conf" ]; then
    cp /etc/nginx/sites-available/nacos.fitness.dualseason.com.conf "$NGINX_DIR/"
    echo -e "  ✓ nacos.fitness.dualseason.com.conf 已复制"
fi

echo -e "\n${YELLOW}[3/4] 更新配置文件路径...${NC}"

# 更新普通配置文件（保持 /home/ubuntu 路径，用于本地部署）
# 这些文件不需要修改

# 更新 Docker 配置文件（改为 Docker 内部路径）
if [ -f "$DOCKER_NGINX_DIR/fitness.dualseason.com.conf" ]; then
    sed -i 's|/home/ubuntu/fitness.dualseason.com_nginx|/etc/nginx/ssl/fitness.dualseason.com_nginx|g' "$DOCKER_NGINX_DIR/fitness.dualseason.com.conf"
    sed -i 's|http://127.0.0.1:8080|http://gateway:8080|g' "$DOCKER_NGINX_DIR/fitness.dualseason.com.conf"
    echo -e "  ✓ Docker fitness 配置已更新"
fi

if [ -f "$DOCKER_NGINX_DIR/minio.fitness.dualseason.com.conf" ]; then
    sed -i 's|/home/ubuntu/minio.fitness.dualseason.com_nginx|/etc/nginx/ssl/minio.fitness.dualseason.com_nginx|g' "$DOCKER_NGINX_DIR/minio.fitness.dualseason.com.conf"
    sed -i 's|http://127.0.0.1:9001|http://minio:9001|g' "$DOCKER_NGINX_DIR/minio.fitness.dualseason.com.conf"
    echo -e "  ✓ Docker minio 配置已更新"
fi

if [ -f "$DOCKER_NGINX_DIR/nacos.fitness.dualseason.com.conf" ]; then
    sed -i 's|/home/ubuntu/nacos.fitness.dualseason.com_nginx|/etc/nginx/ssl/nacos.fitness.dualseason.com_nginx|g' "$DOCKER_NGINX_DIR/nacos.fitness.dualseason.com.conf"
    sed -i 's|http://127.0.0.1:8848|http://nacos:8848|g' "$DOCKER_NGINX_DIR/nacos.fitness.dualseason.com.conf"
    echo -e "  ✓ Docker nacos 配置已更新"
fi

echo -e "\n${YELLOW}[4/4] 生成 Dockerfile...${NC}"

# 创建 Nginx 主配置文件
cat > "$DOCKER_NGINX_DIR/nginx.conf" << 'EOF'
user nginx;
worker_processes auto;
pid /var/run/nginx.pid;

include /etc/nginx/modules-enabled/*.conf;

events {
    worker_connections 768;
}

http {
    sendfile on;
    tcp_nopush on;
    types_hash_max_size 2048;
    server_tokens off;

    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;
    error_log /var/log/nginx/error.log warn;

    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_buffers 16 8k;
    gzip_http_version 1.1;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;

    map $request_method $cors_method {
        OPTIONS "GET, POST, PUT, DELETE, OPTIONS, PATCH";
        default "";
    }

    map $request_method $cors_header {
        OPTIONS "Origin, X-Requested-With, Content-Type, Accept, Authorization, Cache-Control, X-File-Name";
        default "";
    }

    include /etc/nginx/conf.d/*.conf;

    server {
        listen 80;
        server_name localhost;

        location /health {
            access_log off;
            return 200 "healthy\n";
            add_header Content-Type text/plain;
        }
    }
}
EOF

# 创建 Dockerfile
cat > "$DOCKER_NGINX_DIR/Dockerfile" << 'EOF'
FROM nginx:1.25-alpine

RUN apk add --no-cache curl

RUN rm -f /etc/nginx/conf.d/default.conf

RUN mkdir -p /etc/nginx/ssl

COPY nginx.conf /etc/nginx/nginx.conf

COPY fitness.dualseason.com.conf /etc/nginx/conf.d/
COPY minio.fitness.dualseason.com.conf /etc/nginx/conf.d/
COPY nacos.fitness.dualseason.com.conf /etc/nginx/conf.d/

COPY fitness.dualseason.com_nginx /etc/nginx/ssl/fitness.dualseason.com_nginx
COPY minio.fitness.dualseason.com_nginx /etc/nginx/ssl/minio.fitness.dualseason.com_nginx
COPY nacos.fitness.dualseason.com_nginx /etc/nginx/ssl/nacos.fitness.dualseason.com_nginx

RUN chmod 600 /etc/nginx/ssl/*/*.key && \
    chmod 644 /etc/nginx/ssl/*/*.crt

HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:80/health || exit 1

EXPOSE 80 443

CMD ["nginx", "-g", "daemon off;"]
EOF

echo -e "  ✓ Dockerfile 和 nginx.conf 已生成"

echo -e "\n${YELLOW}[5/4] 设置权限...${NC}"

# 设置正确的权限
chmod +x "$NGINX_DIR/deploy.sh"
find "$NGINX_DIR" -name "*.key" -exec chmod 600 {} \;
find "$NGINX_DIR" -name "*.crt" -exec chmod 644 {} \;

echo -e "  ✓ 权限设置完成"

# 完成
echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}✓ 配置同步完成！${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "\n${BLUE}目录结构:${NC}"
echo -e "  nginx/"
echo -e "  ├── deploy.sh                          # 本地部署脚本"
echo -e "  ├── fitness.dualseason.com.conf        # 本地配置文件"
echo -e "  ├── minio.fitness.dualseason.com.conf"
echo -e "  ├── nacos.fitness.dualseason.com.conf"
echo -e "  ├── fitness.dualseason.com_nginx/      # SSL证书"
echo -e "  ├── minio.fitness.dualseason.com_nginx/"
echo -e "  ├── nacos.fitness.dualseason.com_nginx/"
echo -e "  ├── docker/                            # Docker配置"
echo -e "  │   ├── Dockerfile"
echo -e "  │   ├── nginx.conf"
echo -e "  │   └── *.conf + SSL证书"
echo -e "  └── README.md"
echo -e "\n${BLUE}部署方式:${NC}"
echo -e "  1. 本地部署: cd nginx && ./deploy.sh"
echo -e "  2. Docker部署: cd docker && docker-compose up -d"
echo ""
