# Jeez.Fitness CI/CD 自动化部署

基于 Webhook 的自动化部署方案，当 push 到 release 分支时自动构建并部署服务。

## 📋 目录结构

```
ci-cd/
├── README.md              # 本文档
├── simple-webhook.py      # Webhook 服务
├── simple-deploy.sh       # 手动部署脚本
└── simple-install.sh      # 环境安装脚本
```

## 🚀 快速开始

### 1. 环境准备

```bash
# 安装 Python Flask
pip install flask

# 或使用系统包管理器
sudo apt-get install python3-pip
pip3 install flask
```

### 2. 配置 Webhook 密钥

```bash
# 设置环境变量
export WEBHOOK_SECRET="your-secret-key"

# 永久设置
echo 'export WEBHOOK_SECRET="your-secret-key"' >> ~/.bashrc
source ~/.bashrc
```

### 3. 启动 Webhook 服务

```bash
cd /www/jeez.fitness/ci-cd
nohup python3 simple-webhook.py > /dev/null 2>&1 &

# 或使用系统服务（推荐）
sudo ./setup-systemd.sh
```

### 4. 配置 Gitee Webhook

1. 进入 Gitee 仓库 → Settings → Webhooks
2. 添加 Webhook：
   - **URL**: `http://43.136.113.63:9999/deploy`
   - **Secret Token**: 与 `WEBHOOK_SECRET` 一致
   - **Events**: Push
   - **Branch**: `release`

## 📌 日常操作指南

### 查看服务状态

```bash
# 1. 检查 Webhook 进程
ps aux | grep simple-webhook.py

# 2. 检查端口监听
netstat -tlnp | grep 9999
# 或
ss -tlnp | grep 9999

# 3. 查看 Docker 服务状态
docker ps -a
cd /www/jeez.fitness/docker && docker compose ps
```

### 查看日志

```bash
# 1. Webhook 日志
tail -f /www/jeez.fitness/logs/webhook.log

# 2. 部署日志
tail -f /www/jeez.fitness/logs/deploy.log

# 3. Docker 服务日志
# 查看所有服务日志
docker compose logs

# 查看特定服务日志
docker compose logs -f [service-name]
# 例如：
docker compose logs -f gateway
docker compose logs -f mysql

# 4. Nginx 日志
sudo tail -f /var/log/nginx/fitness.dualseason.com.error.log
sudo tail -f /var/log/nginx/fitness.dualseason.com.access.log
```

### 重启服务

```bash
# 1. 重启 Webhook
# 先停止进程
pkill -f simple-webhook.py
# 再启动
cd /www/jeez.fitness/ci-cd
nohup python3 simple-webhook.py > /dev/null 2>&1 &

# 2. 重启 Docker 服务
cd /www/jeez.fitness/docker
docker compose restart [service-name]
# 或重启所有服务
docker compose restart

# 3. 重启 Nginx
sudo nginx -t  # 先测试配置
sudo systemctl reload nginx  # 重载配置
sudo systemctl restart nginx  # 完全重启
```

## 🔍 故障排查

### Webhook 无法部署

```bash
# 1. 检查服务是否运行
ps aux | grep simple-webhook.py

# 2. 检查环境变量
echo $WEBHOOK_SECRET

# 3. 查看 webhook 日志
tail -50 /www/jeez.fitness/logs/webhook.log

# 4. 测试 webhook 连接
curl -X POST "http://127.0.0.1:9999/deploy?token=your-secret-key"
```

### Git 冲突问题

```bash
cd /www/jeez.fitness
git status  # 查看冲突文件

# 解决方案1：放弃本地修改（推荐）
git checkout -- [conflict-file]

# 解决方案2：提交本地修改
git add [conflict-file]
git commit -m "local: 服务器本地修改"
git pull

# 解决方案3：暂存修改
git stash
git pull
git stash pop
```

### Docker 构建失败

```bash
# 1. 查看构建日志
cd /www/jeez.fitness/docker
docker compose build --no-cache

# 2. 查看磁盘空间
df -h

# 3. 清理 Docker 资源
docker system prune -a -f
docker volume prune -f
```

### 服务无法访问

```bash
# 1. 检查端口占用
netstat -tlnp | grep [port]
# 例如：
netstat -tlnp | grep 8080

# 2. 检查容器状态
docker ps | grep [service-name]

# 3. 进入容器调试
docker exec -it [container-name] bash
docker exec -it [container-name] /bin/sh
```

## 🔧 手动部署

当 Webhook 失败时，可以手动部署：

```bash
cd /www/jeez.fitness/ci-cd
./simple-deploy.sh
```

## 📊 性能优化

### 清理日志

```bash
# 1. 清理部署日志（保留最近7天）
find /www/jeez.fitness/logs -name "*.log" -mtime +7 -delete

# 2. 清理 Docker 日志
sudo journalctl --vacuum-time=7d
docker system prune -f
```

### 监控系统资源

```bash
# 1. 系统资源
htop
# 或
top

# 2. 磁盘使用
df -h
du -sh /var/log
du -sh /www/jeez.fitness/logs

# 3. 内存使用
free -h
docker stats
```

## 🔄 自动部署流程

```
1. Push to release branch (Gitee)
   ↓
2. Gitee 发送 webhook 请求
   ↓
3. Webhook 服务接收请求
   ↓
4. 验证密钥和分支
   ↓
5. git pull 拉取最新代码
   ↓
6. docker compose build 构建镜像
   ↓
7. docker compose up -d 启动服务
   ↓
8. 部署完成
```

## 📞 联系支持

如遇到问题，请提供以下信息：
1. 错误日志（`/www/jeez.fitness/logs/`）
2. 服务状态（`docker ps`）
3. 系统信息（`df -h`, `free -h`）

---
最后更新：2024-12-17