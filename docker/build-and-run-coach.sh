#!/bin/bash
# Coach 服务构建脚本

echo "=== Jeez Fitness Coach 服务构建脚本 ==="
echo ""

# 检查是否已构建 jar
if [ ! -f "/www/jeez.fitness/jeez-coach-fitness/target/*-exec.jar" ]; then
    echo "1. 使用 Docker 容器构建 coach 服务..."
    docker run --rm \
      -v /www/jeez.fitness:/app \
      -w /app \
      maven:3.9.5-eclipse-temurin-17 \
      mvn clean package -pl jeez-coach-fitness -am -DskipTests -B
else
    echo "1. JAR 文件已存在，跳过构建..."
fi

echo ""
echo "2. 构建 Docker 镜像..."
docker compose build coach

echo ""
echo "3. 启动 coach 服务..."
docker compose up -d coach

echo ""
echo "4. 查看服务状态..."
docker compose ps coach

echo ""
echo "5. 查看服务日志..."
docker logs --tail 20 jeez-coach

echo ""
echo "=== 构建完成！==="
echo "Coach 服务已启动在端口 8085"