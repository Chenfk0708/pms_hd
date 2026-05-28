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
