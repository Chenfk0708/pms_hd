# Jeez.Fitness 核心链路代码梳理（第一版收尾）

## 1. 文档目的与范围

本文只聚焦你提出的这条主业务链路：

1. 用户注册/登录
2. 用户购会员卡
3. 购卡成功后的人脸录入能力启用
4. 用户人脸录入并落 MinIO
5. 用户到店扫码（或设备识别）开门

说明：本梳理以当前仓库后端代码为准，不包含小程序/前端仓库代码。

---

## 2. 模块与网关路径映射

网关在 `jeez-gateway`，当前主路由是：

- `auth` 服务：外部前缀 `/auth/**` -> auth 服务内部路径 `/**`
- `member` 服务：外部前缀 `/member/**` -> member 服务内部路径 `/**`
- `equipment` 服务：外部前缀 `/equipment/**` -> equipment 服务内部路径 `/**`

所以前端常见调用形态是：

- 认证相关：`/auth/...`
- 会员相关：`/member/api/member/...`
- 设备人脸相关：`/equipment/api/equipment/...`

---

## 3. 主链路业务逻辑（按时序）

## 3.1 注册/登录（实际是“登录即注册”）

### 关键结论

- 当前没有独立“注册”接口。
- 用户首登时在登录接口内自动创建 `jeez_user`（短信登录、邮箱验证码登录、微信登录都支持自动创建）。

### 关键接口

- `POST /auth/sms/send`：发送短信验证码
- `POST /auth/sms/login`：短信验证码登录（不存在用户会自动建号）
- `POST /auth/password/login`：手机号+密码登录
- `POST /auth/wechat/login`：微信小程序登录（支持绑定手机号）
- `POST /auth/password/set`：给已存在账号设置登录密码
- `GET /auth/user/info`：获取当前用户信息（含 `wechatOpenid`）

### 关键代码逻辑

- `AuthService.smsLogin/emailSmsLogin/wechatLogin`：
  - 先校验验证码/微信 code
  - 查用户，不存在则 `userService.createUser(...)`
  - Sa-Token 登录并返回 token
- `UserService.createUser`：
  - 写入 `jeez_user`
  - 默认分配 `guest` 角色

---

## 3.2 购卡与支付

### 关键接口

- `GET /member/api/member/cards/available?storeCode=...`：查询可购卡种
- `GET /member/api/member/cards/types/{cardId}`：查询卡种详情
- `POST /member/api/member/cards/purchase`：创建购卡订单（微信支付）
- `GET /member/api/member/cards/orders/{orderNo}/payment-status`：前端轮询支付状态
- `POST /member/api/member/cards/pay-notify`：微信支付异步回调（无需登录）

### 关键代码逻辑

- `purchaseCard(...)`：
  - 先按 `storeCode` 找门店 ID
  - 校验卡种有效且卡种属于该门店
  - `getOrCreateDbMemberId`：若 `jeez_member` 不存在则自动创建会员主档
  - 落 `jeez_card_purchase_order`（初始 `UNPAID/PENDING`）
  - 调 auth 服务 `/user/info` 拿 `wechatOpenid`
  - 调微信 JSAPI 下单，返回前端支付参数
- 支付成功后：
  - 支付轮询成功或支付回调成功 -> `updateOrderPaymentSuccess(...)`
  - 更新订单状态为 `PAID`
  - 调 `issueMemberCard(...)` 发卡（写入 `jeez_member_card`，状态=1）
  - 订单最终置为 `COMPLETED`

---

## 3.3 人脸录入 + MinIO 存储

### 关键接口

- `POST /equipment/api/equipment/face-sync/upload`（multipart）
  - 参数：`memberId`、`forceOverride`、`file`
- `POST /equipment/api/equipment/face-sync/sync-latest?memberId=...`
- `GET /equipment/api/equipment/face-sync/task-status?taskId=...`
- `POST /equipment/api/equipment/face-sync/retry?taskId=...`

### 关键代码逻辑

- `uploadFaceAndSync(...)`：
  - 校验文件格式仅 JPG/JPEG
  - 接口参数是 `memberId`，服务内部会先解析 `userId`，再按 `user_id` 写 `jeez_user_face`（Base64 + `version` 版本号）
  - 上传 MinIO：对象名 `face/{userId}/{version}/{timestamp}.jpg`
  - 建一个“已保存”任务记录；即使当前无门店/无可用设备，也会创建可查询任务，不阻断上传
- MinIO 失败时：
  - 会降级保留 DB 中 `face_data`，不阻断录入

### 和“购卡成功启用人脸”的关系

- 代码里没有“支付成功后自动调用人脸上传接口”的动作。
- 当前模型是：支付成功后发卡 -> 门禁校验可通过“有效卡”判断放行资格；人脸由前端单独调用上传接口完成录入。

---

## 3.4 到店扫码开门（门禁决策）

### 关键事实

- 这段链路不是前端 HTTP 接口主导，而是设备协议主导。
- 设备通过 WebSocket 连接 `equipment` 服务（端口 `7788`），上报 `sendlog`。

### 协议入口（非 REST）

- 设备上报：`cmd=sendlog`
- 服务处理：`DeviceProtocolHandler.handleSendLog(...)`
- 门禁判定：`DeviceProtocolHandler.evaluateAccessAndSync(deviceSn, latestRecord)`
- 返回设备：`access=1/0 + message + voice`

### 开门判定核心规则

按当前代码，满足以下条件会放行：

1. 设备有效且绑定了门店
2. 能解析出用户与会员关系
3. 该会员在当前门店有“有效会员卡”
   - 条件：`status=1` 且未过期（`expiry_date >= CURDATE()`）
4. 用户存在人脸数据

额外逻辑：

- 若该设备还没同步该用户最新人脸版本，会触发“懒同步”下发命令，但仍可放行（不阻塞进门）。
- 设备记录里 `verifyMode=13` 代表二维码验证（AI 设备）；`mode=8` 为人脸等。

---

## 4. 核心接口清单（给前端对接）

| 阶段 | 方法 | 网关访问路径 | 是否鉴权 | 说明 |
|---|---|---|---|---|
| 登录前 | POST | `/auth/sms/send` | 否 | 发送短信验证码 |
| 登录/注册 | POST | `/auth/sms/login` | 否 | 短信登录，首登自动建号 |
| 登录 | POST | `/auth/password/login` | 否 | 手机号密码登录 |
| 登录 | POST | `/auth/wechat/login` | 否 | 微信登录（可绑定手机号） |
| 账号补全 | POST | `/auth/password/set` | 否 | 短信校验后设置密码 |
| 登录后取资料 | GET | `/auth/user/info` | 是 | 获取用户信息（含 `wechatOpenid`） |
| 选卡 | GET | `/member/api/member/cards/available` | 否 | 按门店查询可购卡种 |
| 看卡详情 | GET | `/member/api/member/cards/types/{cardId}` | 否 | 卡种详情 |
| 下单购卡 | POST | `/member/api/member/cards/purchase` | 是 | 创建购卡订单并拿微信支付参数 |
| 支付轮询 | GET | `/member/api/member/cards/orders/{orderNo}/payment-status` | 是 | 查询/补偿支付状态 |
| 支付回调 | POST | `/member/api/member/cards/pay-notify` | 否 | 微信回调，驱动发卡 |
| 查会员信息 | GET | `/member/api/member/info` | 是 | 取 `memberId` 等（供人脸录入） |
| 上传人脸 | POST | `/equipment/api/equipment/face-sync/upload` | 是（经网关） | 人脸录入并存 MinIO |
| 主动同步最新人脸 | POST | `/equipment/api/equipment/face-sync/sync-latest` | 是（经网关） | 不重新上传，直接触发最新人脸下发 |
| 查同步任务 | GET | `/equipment/api/equipment/face-sync/task-status` | 是（经网关） | 查人脸任务状态 |
| 重试同步 | POST | `/equipment/api/equipment/face-sync/retry` | 是（经网关） | 传 `taskId` 重试失败设备 |

注：开门动作本身为设备协议回包（WebSocket `sendlog` 响应 `access`），不是一个前端直接调用的 REST 开门接口。

---

## 5. 关键数据表（主链路涉及）

- `jeez_user`：账号主体（手机号/邮箱/微信 openid）
- `jeez_member`：会员主档（与 user 关联）
- `jeez_membership_card_type`：卡种定义
- `jeez_card_purchase_order`：购卡订单
- `jeez_member_card`：已发放会员卡
- `jeez_user_face`：用户人脸（Base64 + version）
- `jeez_face_sync_task` / `jeez_face_sync_task_detail`：人脸同步任务
- `jeez_device_face_sync`：设备维度的人脸版本同步状态
- `jeez_records`：门禁/考勤记录

---

## 6. 前后端“深绑定”点（建议重点对齐）

1. 注册不是独立接口，而是登录接口内自动创建用户。
2. 购卡只支持微信支付路径时，前置依赖 `wechatOpenid`。
3. 人脸录入需要前端主动调用；支付成功并不会自动上传人脸。
4. 开门不是“前端调一个 open-door API”，而是设备上报日志后由后端回包 `access` 决策。
5. 门禁校验当前只看“卡有效+未过期+有人脸”，业务若要求“次卡扣次/冻结态限制”，需补充规则。

---

## 7. 当前版本可直接给学生团队的口径

- 你们现在的链路可以统一理解为：
  - `登录即注册` -> `下单购卡并支付` -> `支付回调发卡` -> `前端引导上传人脸` -> `到店由设备协议判定开门`
- 前端若要稳定落地：
  - 一定要做支付状态轮询 + 人脸录入引导
  - 不要把“扫码开门”理解成普通 HTTP 接口，它是设备协议链路

---

## 8. 平台服务首批联调补充（2026-05-27）

### 8.1 启动前提

- 本地平台服务直连数据库：`127.0.0.1:3306/zp_pms`
- 数据库账号：`root / 123456`
- Redis：`127.0.0.1:6379`
- 如果 Docker 容器 `jeez-gateway` 正在运行，会占用本地 `8080`，联调前先执行：

```powershell
docker stop jeez-gateway
```

- 联调结束后如需恢复容器网关，可执行：

```powershell
docker start jeez-gateway
```

### 8.2 本地启动命令

```powershell
$env:SPRING_DATASOURCE_USERNAME='root'
$env:SPRING_DATASOURCE_PASSWORD='123456'
mvn -pl zp-service-platform spring-boot:run
mvn -pl zp-gateway spring-boot:run
```

### 8.3 2026-05-27 实测结果

- 登录账号：`13800000001`
- 演示密码：`demo-login`
- 网关登录：`POST /auth/login` 返回 `code=0`，可拿到 token
- 当前用户：`GET /auth/me` 返回 `userId=12001`、`roleCode=admin`
- 门店列表：`POST /camps/get` 返回 `campId=10001`、`poiId=11001`
- 门店详情：`POST /camp/get` 返回 `name=路客云演示租户`
- 渠道列表：`POST /channels/get` 返回 `accountId` 包含 `25301`（美团）、`25302`（携程）
- 直连平台服务：`POST /edition/resource/get` 在携带 `X-Auth-Verified=true`、`X-User-Id=12001` 时返回 `code=0`

### 8.4 对照数据库核验口径

- `pms_user.user_id=12001` -> `13800000001 / admin@demo.local`
- `pms_camp.camp_id=10001` -> `路客云演示租户`
- `pms_poi.camp_id=10001` -> `poi_id=11001 / 路客云演示门店`
- `channel_account.camp_id=10001` -> `account_id=25301 / 25302`

### 8.5 联调脚本位置

- `zp-service-platform/src/test/resources/http/平台服务首批联调.http`

