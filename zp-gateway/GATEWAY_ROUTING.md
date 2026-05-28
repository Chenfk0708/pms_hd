# zp-gateway 网关路由配置说明

## 📋 微服务列表及端口

| 微服务 | 端口 | 网关路径前缀 | API 直接访问路径 |
|--------|------|-------------|-----------------|
| zp-user | 8090 | `/zp-user/**` | `/api/auth/**`, `/api/users/**` |
| zp-house | 8091 | `/zp-house/**` | `/api/houses/**`, `/api/hotels/**`, `/api/homestays/**`, `/api/hostels/**` |
| zp-inventory | 8092 | `/zp-inventory/**` | - |
| zp-price | 8093 | `/zp-price/**` | - |
| zp-order | 8094 | `/zp-order/**` | `/api/orders/**`, `/api/payments/**` |
| zp-cleaning | 8095 | `/zp-cleaning/**` | - |
| zp-owner | 8096 | `/zp-owner/**` | - |
| zp-channel | 8097 | `/zp-channel/**` | - |

## 🔄 路由规则说明

### 1. 带前缀的路由（StripPrefix=1）
通过网关访问时去掉前缀，例如：
- 请求：`http://localhost:8080/zp-user/api/auth/login`
- 转发到：`http://localhost:8090/api/auth/login`

### 2. API 直接路由（StripPrefix=0）
某些 API 路径可以直接通过网关访问，不需要前缀：
- `/api/auth/**` → zp-user (8090)
- `/api/users/**` → zp-user (8090)
- `/api/houses/**` → zp-house (8091)
- `/api/hotels/**` → zp-house (8091)
- `/api/homestays/**` → zp-house (8091)
- `/api/hostels/**` → zp-house (8091)
- `/api/orders/**` → zp-order (8094)
- `/api/payments/**` → zp-order (8094)

## 🔓 公开访问路径（无需认证）

以下路径不需要登录认证即可访问：

### 认证相关
- `/zp-user/api/auth/register` - 用户注册
- `/zp-user/api/auth/login` - 用户登录
- `/zp-user/api/auth/logout` - 用户登出
- `/api/auth/register` - 用户注册（API直接访问）
- `/api/auth/login` - 用户登录（API直接访问）
- `/api/auth/logout` - 用户登出（API直接访问）

### 房源公开接口
- `/api/houses` - 房源列表
- `/api/houses/**` - 房源详情
- `/api/hotels/**` - 酒店信息
- `/api/homestays/**` - 民宿信息
- `/api/hostels/**` - 青旅信息

### Swagger 文档
- `/swagger-gateway.html` - 统一 Swagger 入口
- `/**/swagger-ui.html` - 各服务 Swagger UI
- `/**/v3/api-docs/**` - API 文档
- `/v3/api-docs-gateway/**` - 网关聚合文档

### 监控端点
- `/actuator/**` - Spring Boot Actuator

### 支付回调
- `/zp-order/api/payments/wechat/notify` - 微信支付回调
- `/zp-order/api/payments/wechat/refund-notify` - 微信退款回调

### 静态资源
- `/jeez-fitness/**` - MinIO 静态资源
- `/css/**`, `/js/**`, `/images/**` - 前端静态资源

## 📝 使用示例

### 示例1：用户登录
```bash
# 通过网关访问（推荐）
curl http://localhost:8080/zp-user/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone": "13800138000", "code": "123456"}'

# 或直接通过 API 路径访问
curl http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone": "13800138000", "code": "123456"}'
```

### 示例2：查询房源列表
```bash
# 通过网关访问
curl http://localhost:8080/zp-house/api/houses

# 或直接通过 API 路径访问（公开接口，无需认证）
curl http://localhost:8080/api/houses
```

### 示例3：创建订单（需要认证）
```bash
# 需要先获取 Token
TOKEN="your-auth-token"

# 通过网关访问
curl http://localhost:8080/zp-order/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"houseId": 1, "checkInDate": "2026-05-10", "checkOutDate": "2026-05-12"}'
```

### 示例4：访问 Swagger 文档
浏览器访问：`http://localhost:8080/swagger-gateway.html`

可以看到所有微服务的 API 文档聚合页面。

## ⚙️ 配置修改

如果需要添加新的微服务或修改路由规则，编辑配置文件：
`zp-gateway/src/main/resources/application.yml`

### 添加新服务步骤：
1. 在 `jeez.gateway.services` 中添加服务地址
2. 在 `spring.cloud.gateway.routes` 中添加路由规则
3. 如需公开访问，在 `jeez.gateway.auth.exclude-paths` 中添加排除路径
4. 在 `springdoc.swagger-ui.urls` 中添加 Swagger 文档地址

## 🐛 常见问题

### Q1: 请求返回 404
**原因：** 路由配置错误或微服务未启动
**解决：** 
- 检查微服务是否在配置的端口运行
- 检查请求路径是否匹配路由规则
- 查看网关日志确认路由匹配情况

### Q2: 请求返回 401 Unauthorized
**原因：** 需要认证但未提供 Token，或 Token 无效
**解决：**
- 确认该接口是否需要认证（查看公开访问路径列表）
- 在请求头中添加 `Authorization: Bearer <token>`
- 检查 Token 是否过期

### Q3: 连接超时
**原因：** 微服务未启动或网络问题
**解决：**
- 确认目标微服务已启动
- 检查防火墙设置
- 验证服务地址和端口配置正确

### Q4: Swagger 文档无法加载
**原因：** 微服务未启动或 SpringDoc 配置问题
**解决：**
- 确保目标微服务已启动
- 检查微服务中是否配置了 SpringDoc
- 访问单个服务的 Swagger：`http://localhost:<port>/swagger-ui.html`

## 📊 架构说明

```
客户端请求
    ↓
zp-gateway (8080)
    ↓
路由匹配 & 认证检查
    ↓
转发到对应微服务
    ↓
zp-user (8090) / zp-house (8091) / zp-order (8094) 等
```

网关负责：
- ✅ 统一入口管理
- ✅ 路由转发
- ✅ 身份认证（Sa-Token）
- ✅ 跨域处理（CORS）
- ✅ API 文档聚合
- ✅ 负载均衡（可扩展）
