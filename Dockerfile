# 多阶段构建 Dockerfile
# 阶段1: 构建阶段
FROM maven:3.9.5-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制 pom 文件
COPY pom.xml .
COPY jeez-common/pom.xml jeez-common/
COPY jeez-auth-fitness/pom.xml jeez-auth-fitness/
COPY jeez-gateway/pom.xml jeez-gateway/
COPY jeez-store-fitness/pom.xml jeez-store-fitness/
COPY jeez-member-fitness/pom.xml jeez-member-fitness/
COPY jeez-equipment-fitness/pom.xml jeez-equipment-fitness/
COPY jeez-coach-fitness/pom.xml jeez-coach-fitness/
COPY jeez-course-fitness/pom.xml jeez-course-fitness/
COPY jeez-manager-fitness/pom.xml jeez-manager-fitness/

# 下载依赖（利用 Docker 缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY . .

# 构建项目
RUN mvn clean package -DskipTests -B

# 阶段2: 运行阶段 - Gateway
FROM eclipse-temurin:17-jre AS gateway
WORKDIR /app
COPY --from=builder /app/jeez-gateway/target/*-exec.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]

# 阶段2: 运行阶段 - Auth Service
FROM eclipse-temurin:17-jre AS auth
WORKDIR /app
COPY --from=builder /app/jeez-auth-fitness/target/*-exec.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]

# 阶段2: 运行阶段 - Member Service
FROM eclipse-temurin:17-jre AS member
WORKDIR /app
COPY --from=builder /app/jeez-member-fitness/target/*-exec.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]

# 阶段2: 运行阶段 - Store Service
FROM eclipse-temurin:17-jre AS store
WORKDIR /app
COPY --from=builder /app/jeez-store-fitness/target/*-exec.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]

# 阶段2: 运行阶段 - Equipment Service
FROM eclipse-temurin:17-jre AS equipment
WORKDIR /app
COPY --from=builder /app/jeez-equipment-fitness/target/*-exec.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]

# 阶段2: 运行阶段 - Coach Service
FROM eclipse-temurin:17-jre AS coach
WORKDIR /app
COPY --from=builder /app/jeez-coach-fitness/target/*-exec.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]

# 阶段2: 运行阶段 - Course Service
FROM eclipse-temurin:17-jre AS course
WORKDIR /app
COPY --from=builder /app/jeez-course-fitness/target/*-exec.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]

# 阶段2: 运行阶段 - Manager Service
FROM eclipse-temurin:17-jre AS manager
WORKDIR /app
COPY --from=builder /app/jeez-manager-fitness/target/*-exec.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]
