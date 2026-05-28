#!/bin/bash
# 极简部署脚本 - 直接执行构建和启动

PROJECT_DIR="/www/jeez.fitness"
DOCKER_DIR="$PROJECT_DIR/docker"
LOG_FILE="$PROJECT_DIR/logs/deploy.log"

# 创建日志目录
mkdir -p "$PROJECT_DIR/logs"

# 日志函数
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

log "========================================"
log "开始部署..."
log "========================================"

# 0. 激进清理策略：删除所有旧容器和镜像，只保留当前使用的
log "【激进清理】停止并删除旧容器..."
docker compose down >> "$LOG_FILE" 2>&1
log "✅ 旧容器已停止"

log "【激进清理】删除所有旧镜像，保留构建使用的基础镜像..."
# 获取当前服务列表
SERVICE_IMAGES=$(cd "$DOCKER_DIR" && docker compose config --images 2>/dev/null | sort -u)

# 删除所有 jeez- 开头的镜像（除了即将构建的）
docker images | grep 'jeez-' | awk '{print $1}' | sort -u | while read repo; do
    # 获取该仓库的所有镜像ID
    ALL_IMAGE_IDS=$(docker images "$repo" --format "{{.ID}}")

    # 删除所有版本（即将重新构建）
    if [ -n "$ALL_IMAGE_IDS" ]; then
        echo "$ALL_IMAGE_IDS" | xargs -r docker rmi -f 2>/dev/null
        log "✅ 已删除 $repo 的所有旧镜像"
    fi
done >> "$LOG_FILE" 2>&1

log "【激进清理】删除停止的容器..."
docker container prune -f >> "$LOG_FILE" 2>&1

log "【激进清理】删除悬空镜像..."
docker image prune -f >> "$LOG_FILE" 2>&1

# log "【激进清理】删除未使用的网络..."
# docker network prune -f >> "$LOG_FILE" 2>&1

# 注意：保留构建缓存以加快构建速度
# log "【激进清理】清理构建缓存..."
# docker builder prune -f >> "$LOG_FILE" 2>&1

# 警告：绝不删除卷！卷中包含数据库数据（MySQL、Redis等）
# log "【激进清理】删除所有未使用的卷..."
# docker volume prune -f >> "$LOG_FILE" 2>&1

log "✅ Docker 激进清理完成（保留构建缓存和数据卷）"

# 显示清理后的资源使用
log "清理后 Docker 资源使用:"
docker system df >> "$LOG_FILE" 2>&1

log "当前磁盘使用: $(df -h / | awk 'NR==2 {print $5}')"

# 1. 拉取代码
log "拉取最新代码..."
cd "$PROJECT_DIR"
if git pull >> "$LOG_FILE" 2>&1; then
    log "✅ 代码拉取成功"
else
    log "❌ git pull失败"
    exit 1
fi

# 2. 构建
log "构建Docker镜像..."
cd "$DOCKER_DIR"
if docker compose build >> "$LOG_FILE" 2>&1; then
    log "✅ 镜像构建成功"
else
    log "❌ build失败"
    exit 1
fi

# 3. 启动
log "启动服务..."
if docker compose up -d >> "$LOG_FILE" 2>&1; then
    log "✅ 服务启动成功"
else
    log "❌ up失败"
    exit 1
fi

log "========================================"
log "✅ 部署完成！"
log "========================================"
docker compose ps | tee -a "$LOG_FILE"
