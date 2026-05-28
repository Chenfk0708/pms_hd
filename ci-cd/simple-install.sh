#!/bin/bash
# 极简安装脚本

echo "📦 安装Python依赖..."
pip3 install flask

echo "📁 创建日志目录..."
mkdir -p /www/jeez.fitness/logs

echo "🔑 设置webhook密钥..."
read -p "请输入webhook密钥（随便输个复杂字符串）: " secret
export WEBHOOK_SECRET="$secret"
echo "export WEBHOOK_SECRET='$secret'" >> ~/.bashrc

echo "🚀 启动webhook服务..."
nohup python3 /www/jeez.fitness/ci-cd/simple-webhook.py > /www/jeez.fitness/logs/webhook-console.log 2>&1 &

echo "✅ 完成！"
echo ""
echo "📋 下一步："
echo "1. 在Gitee仓库设置Webhook："
echo "   URL: http://$(curl -s ifconfig.me):9999/deploy?token=$secret"
echo "   或者使用Header方式: X-Token: $secret"
echo ""
echo "2. 查看日志："
echo "   tail -f /www/jeez.fitness/logs/webhook.log"
echo ""
echo "3. 测试部署："
echo "   curl -X POST -H 'X-Token: $secret' http://localhost:9999/deploy"
