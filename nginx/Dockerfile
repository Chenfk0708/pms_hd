# ============================================================================
# Nginx Dockerfile
# 功能: 为 Jeez 健身房管理系统提供反向代理和 HTTPS 支持
# ============================================================================

FROM nginx:1.25-alpine

# 安装必要的工具
RUN apk add --no-cache curl

# 删除默认配置
RUN rm -f /etc/nginx/conf.d/default.conf

# 创建 SSL 证书目录
RUN mkdir -p /etc/nginx/ssl

# 复制 Nginx 主配置
COPY nginx.conf /etc/nginx/nginx.conf

# 复制所有站点配置
COPY fitness.dualseason.com.conf /etc/nginx/conf.d/
COPY minio.fitness.dualseason.com.conf /etc/nginx/conf.d/
COPY nacos.fitness.dualseason.com.conf /etc/nginx/conf.d/

# 复制 SSL 证书
COPY fitness.dualseason.com_nginx /etc/nginx/ssl/fitness.dualseason.com_nginx
COPY minio.fitness.dualseason.com_nginx /etc/nginx/ssl/minio.fitness.dualseason.com_nginx
COPY nacos.fitness.dualseason.com_nginx /etc/nginx/ssl/nacos.fitness.dualseason.com_nginx

# 设置权限
RUN chmod 600 /etc/nginx/ssl/*/*.key && \
    chmod 644 /etc/nginx/ssl/*/*.crt

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:80/health || exit 1

EXPOSE 80 443

CMD ["nginx", "-g", "daemon off;"]
