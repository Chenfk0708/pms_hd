#!/bin/bash
# 使用 Docker 容器构建 coach 服务

echo "正在构建 coach 服务..."

# 创建临时容器来构建项目
docker run --rm \
  -v /www/jeez.fitness:/app \
  -w /app \
  maven:3.9.5-eclipse-temurin-17 \
  mvn clean package -pl jeez-coach-fitness -am -DskipTests -B

echo "构建完成，检查产物..."
ls -la /www/jeez.fitness/jeez-coach-fitness/target/