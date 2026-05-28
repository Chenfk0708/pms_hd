# zp-gateway 配置更新总结

## ✅ 已完成的修改

### 1. 移除不存在的微服务路由
已删除以下旧服务的配置：
- ❌ auth (认证服务)
- ❌ store (门店服务)
- ❌ member (会员服务)
- ❌ equipment (设备服务)
- ❌ coach (教练服务)
- ❌ course (课程服务)
- ❌ manager (管理服务)
- ❌ order (旧订单服务)

### 2. 保留并优化的微服务路由
当前配置的实际微服务（8个）：

| 微服务 | 端口 | 网关路径 | API直接路径 |
|--------|------|----------|------------|
| zp-user | 8090 | `/zp-user/**` | `/api/auth/**`, `/api/users/**` |
| zp-house | 8091 | `/zp-house/**` | `/api/houses/**`, `/api/hotels/**`, `/api/homestays/**`, `/api/hostels/**` |
| zp-inventory | 8092 | `/zp-inventory/**` | - |
| zp-price | 8093 | `/zp-price/**` | - |
| zp-order | 8094 | `/zp-order/**` | `/api/orders/**`, `/api/payments/**` |
| zp-cleaning | 8095 | `/zp-cleaning/**` | - |
| zp-owner | 8096 | `/zp-owner/**` | - |
| zp-channel | 8097 | `/zp-channel/**` | - |

### 3. 路由规则优化

#### 双重路由支持
为关键服务配置了两种访问方式：

**方式1：带前缀访问（推荐）**
```
http://localhost:8080/zp-user/api/auth/login
→ 转发到 http://localhost:8090/api/auth/login
```

**方式2：API直接访问（更简洁）**
```
http://localhost:8080/api/auth/login
→ 转发到 http://localhost:8090/api/auth/login
```

### 4. 认证排除路径更新
移除了所有不存在服务的路径，只保留：
- ✅ 住拍用户认证接口（注册、登录、登出）
- ✅ 住拍房源公开接口
- ✅ Swagger 文档路径
- ✅ Actuator 监控端点
- ✅ 支付回调接口

### 5. Swagger 文档聚合更新
只聚合存在的8个微服务文档，移除了8个不存在的服务。

## 📁 修改的文件

1. **zp-gateway/src/main/resources/application.yml**
   - 路由配置精简（从 ~200 行减少到 ~150 行）
   - 服务地址配置更新
   - 认证排除路径清理
   - Swagger 文档列表更新

2. **新增文档**
   - `zp-gateway/GATEWAY_ROUTING.md` - 详细的路由配置说明
   - `zp-gateway/test-gateway.ps1` - PowerShell 测试脚本

## 🚀 如何使用

### 启动网关
```bash
# 确保 Redis 已启动
docker-compose up -d redis

# 启动网关
cd zp-gateway
mvn spring-boot:run
```

### 测试路由
```powershell
# 运行测试脚本
cd zp-gateway
.\test-gateway.ps1
```

### 访问示例

**1. 用户登录**
```bash
# 方式1：带前缀
curl http://localhost:8080/zp-user/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone": "13800138000", "code": "123456"}'

# 方式2：API直接访问
curl http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone": "13800138000", "code": "123456"}'
```

**2. 查询房源**
```bash
# 公开接口，无需认证
curl http://localhost:8080/api/houses
```

**3. 创建订单（需要认证）**
```bash
curl http://localhost:8080/zp-order/api/orders \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"houseId": 1, "checkInDate": "2026-05-10"}'
```

**4. 查看 Swagger 文档**
浏览器访问：http://localhost:8080/swagger-gateway.html

## 🔍 验证清单

启动网关后，检查以下项目：

- [ ] 网关在 8080 端口正常启动
- [ ] 可以访问 http://localhost:8080/actuator/health
- [ ] Swagger 文档重定向正常（http://localhost:8080/swagger-ui.html → /swagger-gateway.html）
- [ ] 各微服务路由可访问（需要先启动对应微服务）
- [ ] 公开接口无需 Token 即可访问
- [ ] 受保护接口需要有效 Token

## ⚠️ 注意事项

1. **Redis 必须启动**
   - 网关使用 Redis 存储 Sa-Token 会话
   - 启动命令：`docker-compose up -d redis`

2. **微服务需要按顺序启动**
   - 先启动基础设施（MySQL、Redis）
   - 再启动各个微服务
   - 最后启动网关

3. **端口冲突检查**
   - 确保 8080-8097 端口未被占用
   - 如需修改端口，同时更新网关配置中的服务地址

4. **路由优先级**
   - API 直接路由（StripPrefix=0）优先于带前缀路由
   - 确保路径不冲突

## 🐛 故障排查

### 问题1：网关启动失败
**检查：**
- Redis 是否运行
- 端口 8080 是否被占用
- 配置文件语法是否正确

### 问题2：请求返回 404
**检查：**
- 目标微服务是否启动
- 请求路径是否匹配路由规则
- 查看网关日志确认路由匹配情况

### 问题3：请求返回 503 Service Unavailable
**检查：**
- 目标微服务是否正常运行
- 微服务端口配置是否正确
- 网络连接是否正常

### 问题4：认证失败
**检查：**
- Token 是否有效
- 请求头格式是否正确（`Authorization: Bearer <token>`）
- 该接口是否需要认证

## 📊 架构优势

✅ **统一入口** - 所有请求通过网关，便于管理和监控  
✅ **灵活路由** - 支持多种访问方式，适应不同场景  
✅ **集中认证** - 统一的身份验证和授权  
✅ **API 文档聚合** - 一站式查看所有微服务文档  
✅ **跨域处理** - 统一的 CORS 配置  
✅ **易于扩展** - 添加新服务只需简单配置  

## 📝 后续优化建议

1. **添加限流配置** - 防止恶意请求
2. **添加日志追踪** - 便于问题定位
3. **添加熔断降级** - 提高系统稳定性
4. **添加负载均衡** - 支持多实例部署
5. **添加请求缓存** - 提高响应速度

---

**更新日期：** 2026-05-09  
**更新内容：** 清理不存在的微服务路由，优化网关配置
