# 住拍平台多微服务模块实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `jeez-foundation` 中按 `zp-` 前缀新增住拍平台多微服务模块，基于 `zhupai_database_design.sql` 和 `zhupai_platform_api_doc.md` 实现规范接口，并保持与现有后端模板代码风格一致。

**Architecture:** 采用多 Maven 模块微服务架构，每个 `zp-` 服务独立启动、独立端口、独立 Swagger、独立业务边界，公共响应、分页、工具类先直接复用现有 `jeez-common`。所有服务连接同一个 Docker MySQL 数据库 `zhupai_platform`，通过网关暴露接口，优先把接口路径、入参、出参、字段映射和基础业务流程开发出来，后续再按需要调整公共封装名称或抽象层。

**Tech Stack:** Java 17、Spring Boot 3、Spring Cloud Gateway、MyBatis-Plus、MySQL 8、Redis、Sa-Token、Springdoc OpenAPI、Lombok、Docker Compose。

---

## 1. 当前阶段与确认规则

当前处于阶段 11 已完成：整体联调与收尾阶段。

下一步可进入接口级联调、网关路由完善、Docker 镜像构建或提交整理。

后续每个阶段执行前必须先向用户确认：

- 当前阶段名称。
- 本阶段要新增或修改的模块。
- 本阶段要实现的功能范围。
- 本阶段会使用的现有模板能力。
- 本阶段完成后的验证命令。
- 后续阶段计划。

未经用户确认，不进入下一阶段；未经用户确认，不创建业务代码；未经用户确认，不调整 Docker、网关、根 `pom.xml` 等共享文件。

## 2. HMM 结构化建模

### 2.1 现状 `S0`

- `jeez-foundation` 是多模块 Spring Boot 后端模板。
- 当前已有模块包括 `jeez-auth-fitness`、`jeez-store-fitness`、`jeez-member-fitness`、`jeez-equipment-fitness`、`jeez-coach-fitness`、`jeez-course-fitness`、`jeez-manager-fitness`、`jeez-gateway`、`jeez-common`。
- 现有服务代码风格为 Controller、DTO、Entity、Mapper、Mapper XML、Service、ServiceImpl、Config、Exception 分层。
- 公共响应使用 `com.jeez.common.Result`，分页使用 `com.jeez.common.PageResult`。
- 住拍数据库 `zhupai_platform` 已在 Docker MySQL 中存在。
- 住拍接口文档包含 140 个接口，数据库设计包含 52 张 `zp_` 表。

### 2.2 目标 `Sg`

- 新增多个 `zp-` 开头的 Maven 微服务模块。
- 每个模块职责清晰，能独立启动、独立编译、独立接入 Swagger。
- 所有住拍接口按文档字段与数据库字段映射。
- 保持现有模板的代码组织、命名、注解、异常处理、日志、配置、Mapper XML 风格。
- 生成并维护本计划文档，后续每个阶段执行前先确认。

### 2.3 理想功能 `IFR`

住拍平台业务能力按微服务边界自然扩展，新增功能只影响所属 `zp-` 模块；公共能力先复用 `jeez-common`，网关和 Docker 只承担路由与运行环境职责，业务服务不互相污染。

### 2.4 关键约束 `C`

- 必须使用 `zp-` 前缀新增模块。
- 必须对齐现有模板代码风格。
- 必须基于 `zhupai_database_design.sql` 和 `zhupai_platform_api_doc.md`。
- 数据库使用 Docker 中已存在的 `zhupai_platform`。
- 每个阶段执行前必须等待用户确认。
- 不覆盖当前工作区已有未提交改动。

### 2.5 技术矛盾 `TC` 与物理矛盾 `PC`

- `TC1`：微服务拆分越细，后续扩展越清晰，但初期网关、Docker、跨服务调用和部署配置越多。
- `TC2`：严格匹配接口文档能提升规范性，但会增加 DTO、Mapper XML、分页、聚合查询的实现成本。
- `PC1`：系统既要快速形成可运行闭环，又要在一开始保持可扩展微服务边界。

解决策略：

- 不创建 `zp-common`，先让所有 `zp-` 服务直接依赖 `jeez-common`，减少前期公共抽象成本。
- 先搭建 2 到 3 个核心服务骨架和接口规范基线，确认模板一致性。
- 再按业务域逐步实现接口，避免一次性生成 140 个接口造成不可控差异。
- 跨服务协同先采用清晰的同步接口边界，后续再根据性能和事务需求引入消息或异步任务。

## 3. 模块拆分总览

| 模块 | 服务职责 | 建议端口 | 网关前缀 | 是否有启动类 |
|---|---|---:|---|---|
| `zp-user` | 用户、角色、权限、房东账号、用户类型扩展 | 8090 | `/zp/user/**`、`/api/users/**`、`/api/roles/**` | 是 |
| `zp-house` | 门店、房源、酒店房型、民宿、青旅、媒体、标签 | 8091 | `/zp/house/**`、`/api/houses/**` | 是 |
| `zp-inventory` | 房态、房情表、库存锁、每日入住占用、库存日历 | 8092 | `/zp/inventory/**`、`/api/inventory/**` | 是 |
| `zp-price` | 中央价、门市价、价格规则、价格计划、每日价格 | 8093 | `/zp/price/**`、`/api/prices/**`、`/api/rate-plans/**` | 是 |
| `zp-order` | 订单、排房、入住退房、续住换房、住客、支付退款、售后 | 8094 | `/zp/order/**`、`/api/orders/**` | 是 |
| `zp-cleaning` | 保洁人员、保洁任务、保洁设置、保洁日志、质检 | 8095 | `/zp/cleaning/**`、`/api/cleaning/**` | 是 |
| `zp-owner` | 房东、房源关系、合同、费用、结算 | 8096 | `/zp/owner/**`、`/api/owners/**` | 是 |
| `zp-channel` | OTA 渠道、账号、映射、渠道订单、同步任务、报文日志 | 8097 | `/zp/channel/**`、`/api/channels/**` | 是 |

端口从 `8090` 开始，避开现有 `8080` 到 `8088` 服务和网关配置。

## 4. 数据库表归属

| 模块 | 主归属表 |
|---|---|
| `zp-user` | `zp_user`、`zp_role`、`zp_permission`、`zp_user_role`、`zp_role_permission` |
| `zp-house` | `zp_store`、`zp_house`、`zp_house_facility`、`zp_house_tag`、`zp_house_tag_rel`、`zp_house_media`、`zp_hotel_room_type`、`zp_room`、`zp_hostel_bed`、`zp_homestay_config` |
| `zp-inventory` | `zp_rentable_resource`、`zp_resource_calendar`、`zp_room_status_log`、`zp_inventory_lock`、`zp_reservation_night` |
| `zp-order` | `zp_order`、`zp_order_resource`、`zp_order_guest`、`zp_stay_record`、`zp_order_price_item`、`zp_payment`、`zp_refund`、`zp_after_sale`、`zp_after_sale_record`、`zp_order_log`、`zp_export_task` |
| `zp-price` | `zp_price_rule`、`zp_price_log`、`zp_rate_plan`、`zp_rate_calendar` |
| `zp-cleaning` | `zp_cleaning_staff`、`zp_cleaning_task`、`zp_cleaning_setting`、`zp_cleaning_log` |
| `zp-owner` | `zp_owner`、`zp_owner_house_rel`、`zp_owner_contract`、`zp_house_expense`、`zp_owner_settlement`、`zp_owner_settlement_item` |
| `zp-channel` | `zp_channel`、`zp_channel_account`、`zp_channel_resource_mapping`、`zp_channel_rate_plan_mapping`、`zp_channel_order`、`zp_channel_sync_task`、`zp_channel_message_log` |

共享引用规则：

- 一个表只能有一个主归属模块。
- 非归属模块需要读取关联数据时，优先通过服务接口获取。
- 初期为保证交付速度，可在只读场景使用冗余字段或聚合查询；一旦涉及写操作，必须回到主归属模块。

## 5. 接口编号归属

| 接口编号 | 文档模块 | 归属服务 |
|---|---|---|
| 1 到 22 | 房源基础、酒店房源、民宿房源、青旅房源 | `zp-house` |
| 23 到 32 | 房态管理、房情表、房情表导出 | `zp-inventory` |
| 33 到 37 | 图片、视频、标签 | `zp-house` |
| 38 到 80 | 订单基础、订单类型、排房、入住退房、续住换房、住客、支付退款、售后、统计导出 | `zp-order` |
| 81 到 86 | 房价管理 | `zp-price` |
| 87 到 93 | 保洁任务、人员、统计、设置、日志 | `zp-cleaning` |
| 94 到 96 | 用户、角色、权限 | `zp-user` |
| 97 到 112 | 房东档案、房东房源关系、合同、费用、结算 | `zp-owner` |
| 113 到 126 | 渠道、OTA、映射、渠道订单、同步、报文 | `zp-channel` |
| 127 到 131 | 价格计划、每日价格、推送价格库存到渠道 | `zp-price`，推送动作协同 `zp-channel` 和 `zp-inventory` |
| 132 到 136 | 库存锁、每日入住占用、库存重算 | `zp-inventory` |
| 137 到 138 | 保洁质检、费用联动 | `zp-cleaning`，费用写入协同 `zp-owner` |
| 139 到 140 | 房东账号、用户类型扩展 | `zp-user`，房东数据协同 `zp-owner` |

## 6. 代码风格对齐要求

后续新增 Java 代码必须遵循以下规则：

- 包名采用 `com.jeez.zp.<domain>`，例如 `com.jeez.zp.house`。
- 模块结构采用现有模板风格：`config`、`controller`、`dto.request`、`dto.response`、`entity`、`exception`、`filter`、`mapper`、`service`、`service.impl`、`util`。
- Controller 使用 `@RestController`、`@RequestMapping`、`@RequiredArgsConstructor`、`@Tag`、`@Validated`、`@Slf4j`。
- ServiceImpl 使用 `@Service`、`@RequiredArgsConstructor`、`@Slf4j`，写操作使用 `@Transactional(rollbackFor = Exception.class)`。
- Mapper 接口继承 MyBatis-Plus `BaseMapper<T>`，复杂查询写入 `src/main/resources/mapper/*.xml`。
- Entity 使用 `@TableName`、`@TableId`、`@TableField`，数据库字段保持 `snake_case`，Java 字段保持 `camelCase`。
- DTO 和响应对象与接口文档字段保持一致，外部字段使用 `camelCase`。
- 响应对象优先使用 `com.jeez.common.Result`，分页对象优先使用 `com.jeez.common.PageResult`，保持当前模板统一。
- 不在业务代码中写表结构之外的魔法值，状态值集中放入枚举或常量。
- Java 代码不使用 emoji。
- 代码注释采用简体中文，语义简洁，说明对象、关系、约束和边界。
- 不修改旧 `jeez-*fitness` 业务代码，除非该阶段明确需要接入网关或公共配置。

## 7. 公共能力复用设计

本阶段不创建 `zp-common`，所有住拍服务直接依赖 `jeez-common`。

复用规则：

- 响应封装使用 `com.jeez.common.Result`。
- 分页封装使用 `com.jeez.common.PageResult`。
- 图片、密码、位置、IP、HTTP 等工具类按需复用 `com.jeez.common.utils`。
- MinIO 或图片存储能力按需复用 `com.jeez.common.service.ImageService`。
- 暂不修改 `jeez-common` 中已有类，避免影响旧业务模块。

接口规范处理规则：

- 接口路径、HTTP 方法、请求字段、响应业务字段优先严格对齐 `zhupai_platform_api_doc.md`。
- 响应外壳暂时按现有模板 `Result` 输出，即成功码沿用 `200`，不新增 `traceId` 字段。
- 分页外壳暂时按现有模板 `PageResult` 输出，即 `records/current/size/pages`；业务字段仍按接口文档使用 `camelCase`。
- 如果后续必须完全匹配文档中的 `code=0`、`traceId`、`pageNo/pageSize/total/list`，再统一调整 `jeez-common` 或新增兼容包装层。

这样处理的目标是先把住拍接口按模块开发出来，降低前期公共层重构成本。

## 8. 服务骨架统一文件清单

每个有启动类的 `zp-` 服务统一创建以下文件：

- `<module>/pom.xml`
- `<module>/src/main/java/com/jeez/zp/<domain>/Zp<Domain>Application.java`
- `<module>/src/main/java/com/jeez/zp/<domain>/config/MyBatisPlusConfig.java`
- `<module>/src/main/java/com/jeez/zp/<domain>/config/SwaggerConfig.java`
- `<module>/src/main/java/com/jeez/zp/<domain>/config/JacksonConfig.java`
- `<module>/src/main/java/com/jeez/zp/<domain>/config/SaTokenConfig.java`
- `<module>/src/main/resources/application.yml`
- `<module>/src/main/resources/application-docker.yml`
- `<module>/src/main/resources/mapper/.gitkeep`
- `<module>/src/test/java/com/jeez/zp/<domain>/Zp<Domain>ApplicationTests.java`

统一配置要求：

- `spring.application.name` 使用模块名，例如 `zp-house`。
- `mybatis-plus.mapper-locations` 使用 `classpath*:/mapper/**/*.xml`。
- `mybatis-plus.type-aliases-package` 指向对应 `entity` 包。
- 本地数据源默认指向 `jdbc:mysql://localhost:3306/zhupai_platform`。
- Docker 数据源通过 `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD` 注入。
- Swagger 本地路径保持 `/swagger-ui.html`，网关聚合路径按服务前缀配置。

## 9. 网关与 Docker 接入计划

### 9.1 根工程 `pom.xml`

新增模块顺序：

```xml
<module>zp-user</module>
<module>zp-house</module>
<module>zp-inventory</module>
<module>zp-price</module>
<module>zp-order</module>
<module>zp-cleaning</module>
<module>zp-owner</module>
<module>zp-channel</module>
```

### 9.2 `Dockerfile`

构建阶段新增各 `zp-` 模块 `pom.xml` 复制路径；运行阶段新增各服务 target：

- `zp-user` 暴露 `8090`
- `zp-house` 暴露 `8091`
- `zp-inventory` 暴露 `8092`
- `zp-price` 暴露 `8093`
- `zp-order` 暴露 `8094`
- `zp-cleaning` 暴露 `8095`
- `zp-owner` 暴露 `8096`
- `zp-channel` 暴露 `8097`

### 9.3 `docker/docker-compose.yml`

每个服务新增容器，统一配置：

- `SPRING_PROFILES_ACTIVE=docker`
- `SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/zhupai_platform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true`
- 依赖 `mysql`、`redis`
- 日志挂载到 `./logs/<module>:/app/logs`

### 9.4 `jeez-gateway`

新增 `jeez.gateway.services` 配置：

- `zp-user: http://localhost:8090`
- `zp-house: http://localhost:8091`
- `zp-inventory: http://localhost:8092`
- `zp-price: http://localhost:8093`
- `zp-order: http://localhost:8094`
- `zp-cleaning: http://localhost:8095`
- `zp-owner: http://localhost:8096`
- `zp-channel: http://localhost:8097`

Docker 环境对应容器名：

- `http://zp-user:8090`
- `http://zp-house:8091`
- `http://zp-inventory:8092`
- `http://zp-price:8093`
- `http://zp-order:8094`
- `http://zp-cleaning:8095`
- `http://zp-owner:8096`
- `http://zp-channel:8097`

## 10. 跨服务协同边界

| 触发服务 | 被调用服务 | 场景 | 初期策略 | 后续扩展 |
|---|---|---|---|---|
| `zp-order` | `zp-house` | 创建订单时校验房源、房型、房间、床位 | 同步接口查询 | 服务发现或 OpenFeign |
| `zp-order` | `zp-inventory` | 创建订单、取消订单、排房、退房时锁定或释放库存 | 同步接口写入库存锁 | 引入库存事件 |
| `zp-order` | `zp-price` | 创建订单时计算房费、押金、优惠 | 同步接口查询价格 | 价格快照缓存 |
| `zp-cleaning` | `zp-order` | 退房后生成保洁任务 | 初期由订单服务调用保洁接口 | 消息事件 |
| `zp-price` | `zp-channel` | 推送价格到 OTA 渠道 | 同步调用渠道同步任务 | 异步同步任务 |
| `zp-channel` | `zp-inventory` | 推送库存到 OTA 渠道 | 同步查询库存日历 | 异步重试任务 |
| `zp-user` | `zp-owner` | 房东账号绑定房东档案 | 同步校验房东 | 账号和房东事件 |

写操作边界：

- 库存写入只能由 `zp-inventory` 完成。
- 订单写入只能由 `zp-order` 完成。
- 价格写入只能由 `zp-price` 完成。
- 渠道同步写入只能由 `zp-channel` 完成。
- 房东结算写入只能由 `zp-owner` 完成。

## 11. 分阶段执行计划

### 阶段 1：计划说明文档

**目标：** 固化多微服务拆分方案、表归属、接口归属和确认规则。

**文件：**

- 创建：`docs/zhupai-microservices-module-plan.md`

**验证：**

- 检查文档存在。
- 检查文档不包含未明确的占位内容。
- 用户确认后进入阶段 2。

### 阶段 2：创建核心服务骨架

**目标：** 创建 `zp-user`、`zp-house`、`zp-inventory` 三个核心服务，验证多服务模块结构、配置、Swagger、数据库连接方式，并统一建立接口规范开发基线。

**文件：**

- 创建：`zp-user/**`
- 创建：`zp-house/**`
- 创建：`zp-inventory/**`
- 修改：`pom.xml`
- 修改：`Dockerfile`
- 修改：`docker/docker-compose.yml`
- 修改：`jeez-gateway/src/main/resources/application.yml`
- 修改：`jeez-gateway/src/main/resources/application-docker.yml`

**验证命令：**

```powershell
mvn -pl zp-user,zp-house,zp-inventory -am test
```

**预期结果：**

- 三个服务测试通过。
- 三个服务本地配置指向 `zhupai_platform`。
- 网关配置中出现对应服务路由。
- 三个服务均直接依赖 `jeez-common`，不创建 `zp-common`。

### 阶段 3：实现 `zp-user`

**目标：** 完成接口 94 到 96、139 到 140，建立后台用户、角色、权限、房东账号能力。

**表：**

- `zp_user`
- `zp_role`
- `zp_permission`
- `zp_user_role`
- `zp_role_permission`

**功能：**

- 用户列表。
- 角色列表。
- 配置角色权限。
- 创建房东后台账号。
- 用户列表支持用户类型和房东筛选。

**验证命令：**

```powershell
mvn -pl zp-user -am test
```

### 阶段 4：实现 `zp-house`

**目标：** 完成接口 1 到 22、33 到 37，建立房源基础、酒店、民宿、青旅、媒体标签能力。

**表：**

- `zp_store`
- `zp_house`
- `zp_house_facility`
- `zp_house_tag`
- `zp_house_tag_rel`
- `zp_house_media`
- `zp_hotel_room_type`
- `zp_room`
- `zp_hostel_bed`
- `zp_homestay_config`

**功能：**

- 房源分页、详情、新增、编辑、状态变更、软删除。
- 酒店房型列表、新增、编辑、实体房间列表、新增、状态调整。
- 民宿房间列表、新增、租赁模式、整租配置查询和编辑。
- 青旅房间列表、新增、批量生成床位、床位列表、床位状态调整。
- 媒体上传记录、排序、封面、删除。
- 标签库查询、房源标签设置。

**验证命令：**

```powershell
mvn -pl zp-house -am test
```

### 阶段 5：实现 `zp-inventory`

**目标：** 完成接口 23 到 32、132 到 136，建立房态、房情表、库存锁和每日占用能力。

**表：**

- `zp_rentable_resource`
- `zp_resource_calendar`
- `zp_room_status_log`
- `zp_inventory_lock`
- `zp_reservation_night`

**功能：**

- 月房态查询。
- 日房态查询。
- 人工调整房态。
- 批量锁定和解除锁定。
- 房态日志查询。
- 房情表主查询、房间明细、床位房情表、导出。
- 创建库存锁。
- 释放或确认库存锁。
- 查询库存锁列表。
- 每日入住占用查询。
- 库存日历重算。

**验证命令：**

```powershell
mvn -pl zp-inventory -am test
```

## 13. 阶段执行记录

### 阶段 2 执行记录

- 已新增 `zp-user`、`zp-house`、`zp-inventory` 三个 Maven 模块。
- 已在根 `pom.xml` 中注册三个模块。
- 已验证三个模块可以与 `jeez-common` 一起编译。

### 阶段 3 执行记录

- 已实现 `zp-user` 用户、角色、权限、房东账号接口基线。
- 已修正 `zp-user` 的 MyBatis-Plus 分页拦截器兼容问题，保持与当前模板可编译风格一致。
- 已使用 `D:\maven\apache-maven-3.3.9\bin\mvn.cmd` 验证通过。

### 阶段 4 执行记录

- 已实现 `zp-house` 的房源基础、酒店房型、实体房间、民宿房间、整租配置、青旅宿舍、床位、媒体、标签接口。
- 已新增 `HouseController`、`HotelController`、`HomestayController`、`HostelController` 四个控制器。
- 已新增 `House`、`HouseFacility`、`HouseTag`、`HouseTagRel`、`HouseMedia`、`HotelRoomType`、`Room`、`HostelBed`、`HomestayConfig` 等实体。
- 已新增对应 Mapper 接口和 Mapper XML，分页查询暂采用 SQL `LIMIT/OFFSET`，不依赖 MyBatis-Plus 分页拦截器。
- 已为 `zp-house` 单独配置 Maven Surefire `2.22.2`，确保 JUnit 5 测试可实际执行。
- 已新增 `ZpHouseServiceImplTest`，覆盖房源创建、标签重建、青旅床位批量生成三个关键行为。
- 已执行验证命令并通过：

```powershell
D:\maven\apache-maven-3.3.9\bin\mvn.cmd -f D:\dowork\backen\jeez-foundation\pom.xml -pl zp-house -am test
D:\maven\apache-maven-3.3.9\bin\mvn.cmd -f D:\dowork\backen\jeez-foundation\pom.xml -pl zp-user,zp-house,zp-inventory -am test
```

### 阶段 5 执行记录

- 已实现 `zp-inventory` 的房态管理、房情表、库存锁、每日入住占用、库存日历重算接口基线。
- 已新增 `RoomStatusController`、`RoomBoardController`、`InventoryLockController`、`ReservationNightController`、`ResourceCalendarController` 五个控制器。
- 已新增 `RentableResource`、`ResourceCalendar`、`RoomStatusLog`、`InventoryLock`、`ReservationNight` 五个实体。
- 已新增请求 DTO 和响应 DTO，覆盖接口 23 到 32、132 到 136 的主要入参和出参字段。
- 已新增 `RentableResourceMapper`、`ResourceCalendarMapper`、`RoomStatusLogMapper`、`InventoryLockMapper`、`ReservationNightMapper` 及对应 Mapper XML。
- 房态、房情表和库存锁分页查询继续采用 SQL `LIMIT/OFFSET`，保持与 `zp-house` 当前实现方式一致。
- 库存锁创建按入住离店规则生成开始日包含、结束日不包含的每日锁；真实库 `zp_inventory_lock.lock_no` 是唯一键，因此每个日期行使用独立锁号，并用 `idempotency_key` 作为同一批锁的释放范围。
- 库存重算区分库存锁、预订占用和已入住占用：`LOCKED/CONFIRMED` 锁减少可售库存，`RESERVED` 占用标记为保留，`CHECKED_IN` 占用标记为已入住。
- 已新增 `ZpInventoryServiceImplTest`，覆盖人工调整房态、库存锁日期展开、库存日历重算三个关键行为。
- 已新增 `ZpInventoryDatabaseSmokeTest`，默认跳过；显式传入 `-Dzp.integration=true` 时连接本机 Docker MySQL 的 `zhupai_platform` 并执行库存模块 Mapper SQL。
- 已确认真实数据库连接配置为 `localhost:13306/zhupai_platform`、用户 `zp_user`；当前 `zp_inventory` 目标业务表均可访问，表内数据暂为 0 行。
- 已执行验证命令并通过：

```powershell
D:\maven\apache-maven-3.3.9\bin\mvn.cmd -f D:\dowork\backen\jeez-foundation\pom.xml -pl zp-inventory -am "-Dtest=ZpInventoryServiceImplTest" "-DfailIfNoTests=false" test
D:\maven\apache-maven-3.3.9\bin\mvn.cmd -f D:\dowork\backen\jeez-foundation\pom.xml -pl zp-inventory -am test
D:\maven\apache-maven-3.3.9\bin\mvn.cmd -f D:\dowork\backen\jeez-foundation\pom.xml -pl zp-inventory -am "-Dtest=ZpInventoryDatabaseSmokeTest" "-Dzp.integration=true" "-DfailIfNoTests=false" test
D:\maven\apache-maven-3.3.9\bin\mvn.cmd -f D:\dowork\backen\jeez-foundation\pom.xml -pl zp-user,zp-house,zp-inventory -am test
```

### 阶段 6：实现 `zp-price`

**目标：** 完成接口 81 到 86、127 到 131，建立房价、价格规则、价格计划、每日价格能力。

**表：**

- `zp_price_rule`
- `zp_price_log`
- `zp_rate_plan`
- `zp_rate_calendar`

**功能：**

- 查询中央价。
- 批量设置基础房价。
- 启用或停用价格规则。
- 查询前台门市价。
- 电子房价牌展示数据。
- 查询调价日志。
- 价格计划列表查询。
- 新增或编辑价格计划。
- 查询每日价格日历。
- 批量设置每日价格。
- 推送价格和库存到渠道。

**验证命令：**

```powershell
mvn -pl zp-price -am test
```

### 阶段 6 执行记录

- 已新增 `zp-price` Maven 模块，并在根 `pom.xml` 中注册。
- 已新增 `ZpPriceApplication` 启动类、Swagger、Jackson、Sa-Token、MyBatis-Plus、异常处理和本地/Docker 配置。
- 已实现接口 81 到 86、127 到 131 的接口基线。
- 已新增 `PriceController`、`PriceBoardController`、`PriceLogController`、`RatePlanController`、`RateCalendarController`、`ChannelSyncTaskController` 六个控制器。
- 已新增 `PriceRule`、`PriceLog`、`RatePlan`、`RateCalendar`、`ChannelSyncTask` 五个实体。
- 已新增价格规则、批量基础价、价格规则状态、电子房价牌、调价日志、价格计划、每日价格、渠道价格库存推送等请求和响应 DTO。
- 已新增 `PriceRuleMapper`、`PriceLogMapper`、`RatePlanMapper`、`RateCalendarMapper`、`ChannelSyncTaskMapper` 及对应 Mapper XML。
- 中央价和门市价基于 `zp_price_rule` 区分 `CENTRAL` 和 `RETAIL`。
- 电子房价牌只读关联 `zp_rentable_resource` 和 `zp_resource_calendar`，优先展示每日售卖价快照。
- 每日价格批量设置写入 `zp_rate_calendar`，并同步价格快照到 `zp_resource_calendar`。
- 推送价格库存到渠道先根据 `zp_channel_account` 获取渠道和门店范围，再写入 `zp_channel_sync_task`，后续由 `zp-channel` 接管实际推送执行。
- 按用户最新要求，本阶段只在模块完成后运行一次模块级验证，整体联调留到所有设计模块完成后统一执行。
- 已执行验证命令并通过：

```powershell
D:\maven\apache-maven-3.3.9\bin\mvn.cmd -f D:\dowork\backen\jeez-foundation\pom.xml -pl zp-price -am test
```

### 阶段 7：实现 `zp-order`

**目标：** 完成接口 38 到 80，建立订单全生命周期能力。

**表：**

- `zp_order`
- `zp_order_resource`
- `zp_order_guest`
- `zp_stay_record`
- `zp_order_price_item`
- `zp_payment`
- `zp_refund`
- `zp_after_sale`
- `zp_after_sale_record`
- `zp_order_log`
- `zp_export_task`

**功能：**

- 订单分页、详情、后台创建、取消、备注、日志。
- 全日房、钟点房、长租、青旅床位、民宿分租订单创建。
- 查询可排房间或床位、执行排房、自动排房、取消排房。
- 办理入住、退房、提前退房、延迟退房。
- 今日预抵、今日预离。
- 续住、换房、入住轨迹。
- 入住人列表、新增、修改、移除、证件识别预留接口。
- 价格明细、价格修改、线下收款、支付流水、退款申请、退款审核。
- 售后记录、售后工单、售后状态、售后处理记录。
- 订单统计、经营指标、导出。

**验证命令：**

```powershell
mvn -pl zp-order -am test
```

### 阶段 7 执行记录

- 已新增 `zp-order` Maven 模块，并在根 `pom.xml` 中注册。
- 已新增 `ZpOrderApplication` 启动类、Swagger、Jackson、Sa-Token、MyBatis-Plus、异常处理和本地/Docker 配置。
- 已实现接口 38 到 80 的接口基线，覆盖订单基础、订单类型、排房、入住退房、住客、价格、支付、退款、售后、统计和导出。
- 已新增 `OrderController`、`OrderAssignController`、`OrderStayController`、`OrderGuestController`、`OrderPaymentController`、`OrderAfterSaleController`、`OrderStatisticsController` 七个控制器。
- 已新增 `ZpOrderServiceImpl`，按订单创建、资源绑定、每日入住占用、住客、价格明细、支付流水、退款申请、售后工单、入住轨迹和导出任务形成本模块数据闭环。
- 已新增订单主表、订单资源、入住人、入住轨迹、价格明细、支付、退款、售后、售后处理记录、订单日志、导出任务、每日入住占用等实体和 Mapper。
- 已新增 `OrderMapper.xml`、`OrderResourceMapper.xml`、`OrderGuestMapper.xml`、`StayRecordMapper.xml`、`OrderPriceItemMapper.xml`、`PaymentMapper.xml`、`RefundMapper.xml`、`AfterSaleMapper.xml`、`OrderLogMapper.xml`、`ReservationNightMapper.xml`。
- 跨库存、保洁、渠道的联动暂不直接调用其他服务；当前先写入订单模块所属表和订单日志，后续整体联调时再补服务间协作。
- 订单创建按入住日包含、离店日不包含生成 `zp_reservation_night`；钟点房同日订单至少生成一条占用记录。
- 今日预抵、今日预离使用独立 Mapper 查询，分别按 `check_in_date`、`check_out_date` 精确匹配当天。
- 按用户最新要求，本阶段只在模块完成后运行一次模块级验证，整体联调留到所有设计模块完成后统一执行。
- 已执行验证命令并通过：

```powershell
D:\maven\apache-maven-3.3.9\bin\mvn.cmd -f D:\dowork\backen\jeez-foundation\pom.xml -pl zp-order -am test
```

### 阶段 8：实现 `zp-cleaning`

**目标：** 完成接口 87 到 93、137 到 138，建立保洁和质检能力。

**表：**

- `zp_cleaning_staff`
- `zp_cleaning_task`
- `zp_cleaning_setting`
- `zp_cleaning_log`

**功能：**

- 保洁任务查询、创建、状态更新。
- 保洁人员管理。
- 保洁统计。
- 保洁规则设置。
- 保洁日志查询。
- 提交保洁完成图片和费用。
- 保洁质检审核。

**验证命令：**

```powershell
mvn -pl zp-cleaning -am test
```

### 阶段 8 执行记录

- 已新增 `zp-cleaning` Maven 模块，并在根 `pom.xml` 中注册。
- 已新增 `ZpCleaningApplication` 启动类、Swagger、Jackson、Sa-Token、MyBatis-Plus、异常处理和本地/Docker 配置。
- 已实现接口 87 到 93、137 到 138 的接口基线，覆盖保洁任务、人员、统计、规则、日志、完成提交和质检审核。
- 已新增 `CleaningTaskController`、`CleaningStaffController`、`CleaningStatisticsController`、`CleaningSettingController`、`CleaningLogController` 五个控制器。
- 已新增 `ZpCleaningServiceImpl`，按保洁任务创建、状态流转、完成提交、费用生成、规则保存、质检审核和日志追踪形成本模块数据闭环。
- 已新增 `CleaningTask`、`CleaningStaff`、`CleaningSetting`、`CleaningLog`、`HouseExpense` 五个实体。
- 已新增保洁任务、人员、统计、规则、日志、完成提交、质检审核等请求和响应 DTO。
- 已新增 `CleaningTaskMapper`、`CleaningStaffMapper`、`CleaningSettingMapper`、`CleaningLogMapper`、`HouseExpenseMapper` 及对应 Mapper XML。
- 创建保洁任务时优先从 `zp_rentable_resource` 读取门店和房源归属；请求中显式传入 `storeId`、`houseId` 时可作为兜底。
- 提交保洁完成时写入现场图片、实际完成时间、费用结算状态；当 `generateExpense=true` 且费用大于 0 时生成 `zp_house_expense` 保洁费用。
- 保洁质检审核更新 `quality_status`，驳回原因和质检图片写入 `zp_cleaning_log`，避免新增非设计字段。
- 按用户最新要求，本阶段只在模块完成后运行一次模块级验证，整体联调留到所有设计模块完成后统一执行。
- 已执行验证命令并通过：

```powershell
D:\maven\apache-maven-3.3.9\bin\mvn.cmd -f D:\dowork\backen\jeez-foundation\pom.xml -pl zp-cleaning -am test
```

### 阶段 9：实现 `zp-owner`

**目标：** 完成接口 97 到 112，建立房东、合同、费用、结算能力。

**表：**

- `zp_owner`
- `zp_owner_house_rel`
- `zp_owner_contract`
- `zp_house_expense`
- `zp_owner_settlement`
- `zp_owner_settlement_item`

**功能：**

- 房东列表、新增、详情、编辑、启停。
- 绑定房东与房源。
- 查询房东房源关系。
- 合同列表、新增或编辑、审核、终止、续签。
- 房源费用列表、新增费用。
- 房东结算单列表、生成、详情、确认、打款。

**验证命令：**

```powershell
mvn -pl zp-owner -am test
```

### 阶段 9 执行记录

- 已新增 `zp-owner` Maven 模块，并在根 `pom.xml` 中注册。
- 已新增 `ZpOwnerApplication` 启动类、Swagger、Jackson、Sa-Token、MyBatis-Plus、异常处理和本地/Docker 配置。
- 已实现接口 97 到 112 的接口基线，覆盖房东档案、房东房源关系、合同、费用和结算。
- 已新增 `OwnerController`、`OwnerHouseRelationController`、`OwnerContractController`、`HouseExpenseController`、`OwnerSettlementController` 五个控制器。
- 已新增 `ZpOwnerServiceImpl`，按房东创建、关系绑定、合同状态流转、费用入账、结算生成、结算确认和打款形成本模块数据闭环。
- 已新增 `Owner`、`OwnerHouseRelation`、`OwnerContract`、`HouseExpense`、`OwnerSettlement`、`OwnerSettlementItem` 六个实体。
- 已新增房东、房东房源关系、合同、房源费用、房东结算等请求和响应 DTO。
- 已新增 `OwnerMapper`、`OwnerHouseRelationMapper`、`OwnerContractMapper`、`HouseExpenseMapper`、`OwnerSettlementMapper`、`OwnerSettlementItemMapper` 及对应 Mapper XML。
- 房东结算生成按结算周期汇总订单收入、退款金额、未结算费用和当前有效合同佣金比例，生成结算单及收入、佣金、费用、退款四类明细。
- 结算确认或打款通过状态接口完成，打款账户快照写入 `zp_owner_settlement.payout_account`，详情查询时还原为对象返回。
- 按用户最新要求，本阶段只在模块完成后运行一次模块级验证，整体联调留到所有设计模块完成后统一执行。
- 已执行验证命令并通过：

```powershell
D:\maven\apache-maven-3.3.9\bin\mvn.cmd -f D:\dowork\backen\jeez-foundation\pom.xml -pl zp-owner -am test
```

### 阶段 10：实现 `zp-channel`

**目标：** 完成接口 113 到 126，建立 OTA 渠道和同步能力。

**表：**

- `zp_channel`
- `zp_channel_account`
- `zp_channel_resource_mapping`
- `zp_channel_rate_plan_mapping`
- `zp_channel_order`
- `zp_channel_sync_task`
- `zp_channel_message_log`

**功能：**

- 渠道列表、新增或编辑。
- 渠道账号列表、新增或编辑。
- 渠道资源映射查询、新增或编辑。
- 渠道价格计划映射查询、新增或编辑。
- 渠道订单列表、详情、绑定内部订单。
- 创建渠道同步任务。
- 查询或重试渠道同步任务。
- 渠道接口报文日志查询。

**验证命令：**

```powershell
mvn -pl zp-channel -am test
```

### 阶段 10 执行记录

- 已新增 `zp-channel` Maven 模块，并在根 `pom.xml` 中注册。
- 已新增 `ZpChannelApplication` 启动类、Swagger、Jackson、Sa-Token、MyBatis-Plus、异常处理和本地/Docker 配置。
- 已实现接口 113 到 126 的接口基线，覆盖渠道、渠道账号、资源映射、价格计划映射、渠道订单、同步任务和报文日志。
- 已新增 `ChannelController`、`ChannelAccountController`、`ChannelMappingController`、`ChannelOrderController`、`ChannelSyncTaskController`、`ChannelMessageLogController` 六个控制器。
- 已新增 `ZpChannelServiceImpl`，按渠道维护、账号维护、映射保存、订单绑定、同步任务创建与重试、报文查询形成本模块数据闭环。
- 已新增 `Channel`、`ChannelAccount`、`ChannelResourceMapping`、`ChannelRatePlanMapping`、`ChannelOrder`、`ChannelSyncTask`、`ChannelMessageLog` 七个实体。
- 已新增渠道、账号、映射、订单、同步任务和报文日志请求与响应 DTO。
- 已新增 `ChannelMapper`、`ChannelAccountMapper`、`ChannelResourceMappingMapper`、`ChannelRatePlanMappingMapper`、`ChannelOrderMapper`、`ChannelSyncTaskMapper`、`ChannelMessageLogMapper` 及对应 Mapper XML。
- 渠道账号密钥按接口文档只保存 `secretRef`，不在接口响应中返回明文密钥。
- 渠道订单绑定本阶段先支持绑定已有内部订单；自动创建内部订单涉及 `zp-order` 写边界，保留到整体联调阶段通过服务协作补齐。
- 渠道同步任务创建后初始状态为 `WAITING`，重试接口将任务流转为 `RETRY` 并增加 `retry_count`。
- 已新增 `ZpChannelServiceImplTest`，覆盖同步任务重试状态流转。
- 按用户最新要求，本阶段只在模块完成后运行一次模块级验证，整体联调留到所有设计模块完成后统一执行。
- 已执行验证命令并通过：

```powershell
D:\maven\apache-maven-3.3.9\bin\mvn.cmd -f D:\dowork\backen\jeez-foundation\pom.xml -pl zp-channel -am test
```

### 阶段 10.1：实现 `zp-user` 登录注册认证

**目标：** 基于真实 `zhupai_platform.zp_user` 表和 Sa-Token，为住拍后台用户提供登录、注册、登出和当前用户信息接口。

**表：**

- `zp_user`
- `zp_user_role`
- `zp_role`
- `zp_role_permission`
- `zp_permission`

**功能：**

- 用户注册。
- 用户名密码登录。
- Sa-Token 登录态生成和登出。
- 查询当前登录用户信息。
- 登录后返回角色编码和权限编码。
- 更新最后登录时间。

**验证命令：**

```powershell
mvn -pl zp-user -am -DfailIfNoTests=false test
```

### 阶段 10.1 执行记录

- 已在 `zp-user` 新增 `/api/auth/register`、`/api/auth/login`、`/api/auth/logout`、`/api/auth/me` 四个认证接口。
- 已新增 `AuthController`、`ZpAuthService`、`ZpAuthServiceImpl`。
- 已新增 `AuthSessionManager` 和 `SaTokenAuthSessionManager`，将 Sa-Token 静态调用隔离到会话管理器，便于单元测试和后续扩展。
- 已新增 `AuthRegisterRequest`、`AuthLoginRequest`、`AuthUserInfoResponse`、`AuthLoginResponse`、`AuthOperationResponse`。
- 注册接口基于 `zp_user.username` 唯一约束创建用户，密码使用 `jeez-common.PasswordUtil` 的 BCrypt 哈希写入 `password_hash`。
- 登录接口按 `username` 查询真实 `zp_user`，校验 `password_hash`、`status=ENABLED`、`deleted_at IS NULL`，成功后通过 Sa-Token 生成 token，并更新 `last_login_at`。
- 当前用户信息接口返回用户基础信息、角色编码和权限编码。
- 已扩展 `UserMapper`、`RoleMapper`、`PermissionMapper` 及对应 XML，支持认证用户查询、最后登录时间更新、角色编码查询和权限编码查询。
- 已调整 `zp-user` 的 Sa-Token 放行路径，允许未登录访问 `/api/auth/register` 和 `/api/auth/login`。
- 已将 `zp-user` 本地配置对齐当前 Docker MySQL：`localhost:13306/zhupai_platform`、用户 `zp_user`；Redis 对齐 `localhost:16379`。
- 已新增 `ZpAuthServiceImplTest`，采用测试先行方式覆盖注册写入、登录成功、禁用用户拒绝三个认证行为。
- 已执行验证命令并通过：

```powershell
D:\maven\apache-maven-3.3.9\bin\mvn.cmd -f D:\dowork\backen\jeez-foundation\pom.xml -pl zp-user -am -Dtest=ZpAuthServiceImplTest -DfailIfNoTests=false test
D:\maven\apache-maven-3.3.9\bin\mvn.cmd -f D:\dowork\backen\jeez-foundation\pom.xml -pl zp-user -am -DfailIfNoTests=false test
```

### 阶段 11：整体联调与收尾

**目标：** 验证所有 `zp-` 模块可编译、可启动、可通过网关访问 Swagger 和核心接口。

**验证命令：**

```powershell
mvn -pl zp-user,zp-house,zp-inventory,zp-price,zp-order,zp-cleaning,zp-owner,zp-channel -am test
```

Docker 验证命令：

```powershell
docker compose -f docker/docker-compose.yml ps
```

最终检查：

- 所有新增模块可编译。
- 每个服务都有独立 Swagger。
- 网关路由能转发到对应服务。
- 接口字段与文档保持 `camelCase`。
- 数据库字段与 SQL 设计保持 `snake_case`。
- 每个写接口都有事务边界。
- 每个软删除接口只更新删除字段，不物理删除业务数据。

### 阶段 11 执行记录

- 已执行全部新增 `zp-` 模块的 Maven 整体验证。
- 验证范围包含：`zp-user`、`zp-house`、`zp-inventory`、`zp-price`、`zp-order`、`zp-cleaning`、`zp-owner`、`zp-channel`。
- Maven Reactor 结果显示 `jeez-common` 和所有 `zp-` 模块均为 `SUCCESS`。
- 测试结果汇总：
  - `zp-user`：7 个测试，0 失败，0 错误。
  - `zp-house`：4 个测试，0 失败，0 错误。
  - `zp-inventory`：5 个测试，0 失败，0 错误，1 个数据库冒烟测试默认跳过。
  - `zp-price`：1 个测试，0 失败，0 错误。
  - `zp-channel`：1 个测试，0 失败，0 错误。
  - `zp-order`、`zp-cleaning`、`zp-owner` 当前无测试源码，本次已完成主代码编译验证。
- 已确认 Docker 中 `zp-mysql` 和 `zp-redis` 均为 healthy 状态。
- 已确认 Docker 中住拍相关服务容器正在运行：`zp-auth-service`、`zp-house-service`、`zp-inventory-service`、`zp-order-service`、`zp-price-service`、`zp-cleaning-service`、`zp-owner-service`、`zp-channel-service`、`zp-gateway`。
- 已通过 MySQL 容器确认真实数据库 `zhupai_platform` 可访问，当前存在 52 张 `zp_` 业务表。
- 已确认核心表 `zp_user`、`zp_channel`、`zp_owner` 可访问。
- 已执行验证命令并通过：

```powershell
D:\maven\apache-maven-3.3.9\bin\mvn.cmd -f D:\dowork\backen\jeez-foundation\pom.xml -pl zp-user,zp-house,zp-inventory,zp-price,zp-order,zp-cleaning,zp-owner,zp-channel -am -DfailIfNoTests=false test
docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}"
docker exec zp-mysql mysql -uzp_user -pzp_pass_123456 -D zhupai_platform -e "SELECT DATABASE() AS db_name; SELECT COUNT(*) AS zp_table_count FROM information_schema.tables WHERE table_schema='zhupai_platform' AND table_name LIKE 'zp_%'; SHOW TABLES LIKE 'zp_user'; SHOW TABLES LIKE 'zp_channel'; SHOW TABLES LIKE 'zp_owner';"
```

## 12. 阶段执行前确认模板

后续每次执行前先输出以下内容并等待确认：

```text
当前阶段：
本阶段模块：
本阶段功能：
本阶段使用的模板能力：
本阶段计划修改文件：
本阶段验证命令：
后续阶段：
请确认是否执行。
```

## 13. 风险与控制

| 风险 | 影响 | 控制方式 |
|---|---|---|
| 一次性生成接口过多 | 风格不一致、难以验证 | 按服务分阶段实现，每阶段独立编译和确认 |
| 微服务拆分后跨服务调用复杂 | 订单、库存、价格、渠道链路容易耦合 | 先明确主归属表和写操作边界 |
| 现有工作区已有未提交修改 | 可能误覆盖用户改动 | 只修改本阶段确认文件，必要时先展示差异 |
| 接口文档响应码与旧模板响应码不同 | 文档外壳与模板外壳存在差异 | 前期按 `jeez-common.Result` 开发接口，后续需要完全匹配时再统一调整公共层 |
| 数据库软删除字段不完全一致 | 查询条件遗漏已删除数据过滤 | 每张表实体按 SQL 字段单独映射，不强行套用旧基础实体 |
| 网关 `/api/**` 路由冲突 | 请求转发到错误服务 | 按具体路径优先配置，避免泛化 `/api/**` |

## 14. 本轮评价

```xml
<自打分>
  <数值 类型="integer">94</数值>
  <扣分理由>计划已改为直接复用 jeez-common 并优先开发接口规范，但尚未进入代码实现阶段，实际编译和 Docker 运行兼容性需要后续验证。</扣分理由>
</自打分>
```
