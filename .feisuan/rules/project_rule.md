
# 开发规范指南 (Jeez-Fitness 项目)

为保证代码质量、可维护性、安全性与可扩展性，请在开发过程中严格遵循以下规范。本规范基于 Spring Boot 3.x、JDK 17 及 MyBatis-Plus 技术栈制定。

## 一、项目环境与基础配置

### 1.1 开发环境信息
- **操作系统**：Windows 11
- **工作目录**：`D:\dowork\backen\jeez-foundation`
- **当前时间**：2026-05-09 11:11:04
- **代码作者**：Administrator

### 1.2 技术栈与版本
- **语言版本**：Java 17
- **主框架**：Spring Boot 3.x
- **构建工具**：Maven
- **核心依赖**：
  - `mybatis-plus-boot-starter` (数据持久化)
  - `mysql-connector-j` (数据库驱动)
  - `sa-token-spring-boot3-starter` (权限认证)
  - `sa-token-redis-jackson` (Token 存储)
  - `springdoc-openapi-starter-webmvc-ui` (API 文档)
  - `lombok` (代码简化)
  - `spring-boot-starter-data-redis` (缓存支持)

## 二、目录结构规范

项目采用多模块 Maven 结构，主目录下包含多个微服务模块。

```text
jeez-foundation
├── jeez-gateway/                 # 网关服务 (端口 8080)
├── jeez-auth-fitness/            # 认证服务
├── jeez-manager-fitness/         # 管理后台服务 (端口 8087)
├── jeez-store-fitness/           # 门店管理服务 (端口 8083)
├── jeez-member-fitness/          # 会员服务 (端口 8082)
├── jeez-coach-fitness/           # 教练服务 (端口 8085)
├── jeez-equipment-fitness/       # 设备服务
├── jeez-course-fitness/          # 课程服务
├── zp-user/                      # 住拍用户服务 (端口 8090)
├── zp-house/                     # 住拍房源服务
├── zp-inventory/                 # 住拍库存服务
├── zp-price/                     # 住拍价格服务
├── zp-order/                     # 住拍订单服务
├── zp-cleaning/                  # 住拍保洁服务
├── zp-owner/                     # 住拍业主服务
├── zp-channel/                   # 住拍渠道服务
├── jeez-common/                  # 公共模块
└── nginx/                        # Nginx 部署配置
```

**推荐子模块目录结构** (以 `zp-user` 为例)：
```text
zp-user/
└── src
    └── main
        ├── java/com/jeez/zp/user/
        │   ├── auth/              # 认证相关逻辑
        │   ├── config/            # 配置类
        │   ├── controller/        # 控制器层
        │   ├── dto/
        │   │   ├── request/       # 请求 DTO
        │   │   └── response/      # 响应 DTO
        │   ├── entity/            # 实体类
        │   ├── exception/         # 异常处理
        │   ├── mapper/            # MyBatis-Plus Mapper
        │   └── service/
        │       └── impl/          # 业务逻辑实现类
        └── resources/
            └── mapper/            # XML 映射文件
```

## 三、分层架构规范

| 层级        | 职责说明                         | 开发约束与注意事项                                               |
|-------------|----------------------------------|------------------------------------------------------------------|
| **Controller** | 处理 HTTP 请求与响应，定义 API 接口 | 不得直接访问数据库，必须通过 Service 层调用                        |
| **Service**    | 实现业务逻辑、事务管理与数据校验   | 必须通过 Mapper 层访问数据库；返回 DTO 而非 Entity（除非必要）     |
| **Mapper**     | 数据库访问与持久化操作             | 继承 `BaseMapper`；使用 `@TableName` 注解指定表名                 |
| **Entity**     | 映射数据库表结构                   | 不得直接返回给前端（需转换为 DTO）；包名统一为 `entity`            |
| **DTO**        | 数据传输对象                       | 分为 `request` (入参) 和 `response` (出参)，使用 Lombok 简化        |

### 接口与实现分离
- 所有业务逻辑通过接口定义，具体实现放在接口所在包下的 `impl` 子包中。

## 四、安全与性能规范

### 4.1 输入校验
- 使用 `@Valid` 与 JSR-303 校验注解（如 `@NotBlank`, `@Size` 等）
  - 注意：Spring Boot 3.x 中校验注解位于 `jakarta.validation.constraints.*`

### 4.2 防范攻击
- **禁止**手动拼接 SQL 字符串，防止 SQL 注入攻击。
- **禁止**在代码中直接存储明文密码，需使用 `BCryptPasswordEncoder` 加密。

### 4.3 事务管理
- `@Transactional` 注解仅用于 **Service 层**方法。
- 避免在循环中频繁提交事务，影响性能。

## 五、MyBatis-Plus 规范

### 5.1 配置规范
- **命名策略**：开启驼峰命名自动转换 (`map-underscore-to-camel-case: true`)。
- **主键策略**：全局配置为 `AUTO` (自增) 或 `ASSIGN_ID` (雪花算法)。
- **Mapper XML 位置**：`classpath*:/mapper/**/*.xml`。

### 5.2 实体类规范
- 必须添加 `@TableName` 注解对应数据库表名。
- 使用逻辑删除：`@TableLogic` 注解标记 `deleted` 字段，默认值为 `1` (已删除) 或 `0` (未删除)。

## 六、代码风格规范

### 6.1 命名规范
| 类型       | 命名方式             | 示例                  |
|------------|----------------------|-----------------------|
| 类名       | UpperCamelCase       | `UserServiceImpl`     |
| 方法/变量  | lowerCamelCase       | `saveUser()`          |
| 常量       | UPPER_SNAKE_CASE     | `MAX_LOGIN_ATTEMPTS`  |
| 包名       | 全小写，点分隔       | `com.jeez.zp.user`     |

### 6.2 注释规范
- 所有类、方法、字段需添加 **Javadoc** 注释。
- 注释语言：**中文** (与项目第一语言一致)。

### 6.3 类型命名规范（阿里巴巴风格）
| 后缀 | 用途说明                     | 示例         |
|------|------------------------------|--------------|
| DTO  | 数据传输对象                 | `UserDTO`    |
| DO   | 数据库实体对象               | `UserDO`     |
| VO   | 视图展示对象                 | `UserVO`     |

### 6.4 实体类简化工具
- 使用 Lombok 注解替代手动编写 getter/setter/构造方法：
  - `@Data`
  - `@NoArgsConstructor`
  - `@AllArgsConstructor`

## 七、扩展性与日志规范

### 7.1 接口优先原则
- 所有业务逻辑通过接口定义，具体实现放在 `impl` 包中。

### 7.2 日志记录
- 使用 `@Slf4j` 注解代替 `System.out.println`。
- **日志级别**：`debug` 用于开发排错，`info` 用于关键流程，`error` 用于异常捕获。

### 7.3 异常处理
- 统一使用自定义异常类，避免使用原始 `throw new RuntimeException`。
- 异常信息需包含业务上下文，方便排查问题。

## 八、编码原则总结

| 原则       | 说明                                       |
|------------|--------------------------------------------|
| **SOLID**  | 高内聚、低耦合，增强可维护性与可扩展性     |
| **DRY**    | 避免重复代码，提高复用性                   |
| **KISS**   | 保持代码简洁易懂                           |
| **YAGNI**  | 不实现当前不需要的功能                     |
| **OWASP**  | 防范常见安全漏洞，如 SQL 注入、XSS 等      |
