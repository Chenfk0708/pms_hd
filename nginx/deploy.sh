# ============================================================================
# Nginx 部署脚本（支持 fitness、minio、nacos 三个域名）
# 用法: ./deploy.sh
# 说明: 在服务器上运行，从当前目录部署 Nginx 配置和 SSL 证书
# ============================================================================

set -e

# 颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 配置
DOMAINS=("fitness.dualseason.com" "minio.fitness.dualseason.com" "nacos.fitness.dualseason.com")
NGINX_CONF_DIR="/etc/nginx/sites-available"
NGINX_ENABLED_DIR="/etc/nginx/sites-enabled"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Nginx 本地部署脚本${NC}"
echo -e "${BLUE}========================================${NC}"

# 检查文件
echo -e "\n${YELLOW}[1/4] 检查文件...${NC}"
for domain in "${DOMAINS[@]}"; do
    conf_file="${domain}.conf"
    ssl_dir="${domain}_nginx"
    
    [ ! -f "$conf_file" ] && echo -e "${RED}错误: 找不到 $conf_file${NC}" && exit 1
    [ ! -d "$ssl_dir" ] && echo -e "${RED}错误: 找不到 $ssl_dir 目录${NC}" && exit 1
    echo -e "  ✓ $domain"
done
echo -e "${GREEN}✓ 文件检查通过${NC}"

# 部署 SSL 证书
echo -e "\n${YELLOW}[2/4] 部署 SSL 证书...${NC}"
for domain in "${DOMAINS[@]}"; do
    ssl_dir="${domain}_nginx"
    target_ssl_dir="/home/ubuntu/${ssl_dir}"
    
    sudo mkdir -p "$target_ssl_dir"
    sudo cp "$ssl_dir"/* "$target_ssl_dir/"
    sudo chmod 600 "$target_ssl_dir"/*.key
    sudo chmod 644 "$target_ssl_dir"/*.crt
    echo -e "  ✓ $domain SSL 证书部署完成"
done
echo -e "${GREEN}✓ SSL 证书部署完成${NC}"

# 部署 Nginx 配置
echo -e "\n${YELLOW}[3/4] 部署 Nginx 配置...${NC}"
for domain in "${DOMAINS[@]}"; do
    conf_file="${domain}.conf"
    
    # 备份旧配置
    if [ -f "${NGINX_CONF_DIR}/${domain}.conf" ]; then
        sudo cp "${NGINX_CONF_DIR}/${domain}.conf" \
                "${NGINX_CONF_DIR}/${domain}.conf.backup.$(date +%Y%m%d_%H%M%S)"
        echo -e "  ✓ $domain 备份旧配置完成"
    fi
    
    # 部署新配置
    sudo cp "$conf_file" "${NGINX_CONF_DIR}/${domain}.conf"
    sudo ln -sf "${NGINX_CONF_DIR}/${domain}.conf" "${NGINX_ENABLED_DIR}/"
    echo -e "  ✓ $domain 配置部署完成"
done
echo -e "${GREEN}✓ Nginx 配置部署完成${NC}"

# 验证配置
echo -e "\n${YELLOW}[4/4] 验证并重载 Nginx...${NC}"
if ! sudo nginx -t; then
    echo -e "${RED}Nginx 配置验证失败${NC}"
    exit 1
fi

sudo systemctl reload nginx
echo -e "${GREEN}✓ Nginx 重载成功${NC}"

# 完成
echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}✓ 部署完成！${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "\n${BLUE}访问地址:${NC}"
echo -e "  • Fitness: https://fitness.dualseason.com"
echo -e "  • MinIO:   https://minio.fitness.dualseason.com"
echo -e "  • Nacos:   https://nacos.fitness.dualseason.com"
echo -e "\n${BLUE}API 文档:${NC}"
echo -e "  • https://fitness.dualseason.com/swagger-gateway.html"
echo ""
