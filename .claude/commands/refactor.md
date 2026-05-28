# Jeez Fitness 项目统一重写命令

你现在是 Jeez Fitness 项目的代码重构专家。你的任务是将各模块的技术实现统一规范化，保留业务逻辑，重写技术部分，并遵循标准的 Git 工作流。

## 重要参考资源

- **数据库结构参考**: `D:\Code\jeez.fitness\docker\init-sql\01-init-data.sql`
- **公共模块**: `jeez-common` - 所有业务模块都依赖此模块

---

## Git 工作流（必须遵守）

### 分支策略
重构代码使用 **refactor 分支**:
```
refactor/xxx ──> develop ──> release ──> production
```

### 执行流程
1. **从 develop 创建 refactor 分支**
   ```bash
   git checkout develop
   git pull
   git checkout -b refactor/模块名或描述
   ```

2. **重构完成后提交代码**
   ```bash
   git add .
   git commit -m "refactor(模块): 重构描述"
   git push origin refactor/xxx
   ```

3. **合并到 develop**
   ```bash
   git checkout develop
   git pull
   git merge refactor/xxx --no-ff
   ```

4. **全面编译验证**
   - 重构可能影响多个模块，需编译所有相关模块
   - 如果修改了 common: `mvn clean install -DskipTests -pl jeez-common`
   - 编译所有业务模块确保无影响

5. **推送 develop**
   ```bash
   git push origin develop
   ```

---

## 项目概述

Jeez Fitness 是一个基于 Spring Cloud 微服务架构的健身房管理系统，包含以下模块：

| 模块 | 端口 | 职责 |
|------|------|------|
| jeez-gateway | 8080 | API网关，路由转发，聚合Swagger |
| jeez-auth-fitness | 8081 | 认证服务，用户管理，权限控制 |
| jeez-member-fitness | 8082 | 会员服务 |
| jeez-store-fitness | 8083 | 门店服务 |
| jeez-equipment-fitness | 8084 | 设备服务 |
| jeez-coach-fitness | 8085 | 教练服务 |
| jeez-course-fitness | 8086 | 课程服务 |
| jeez-manager-fitness | 8087 | 管理服务 |
| jeez-order-fitness | 8088 | 订单支付服务 |
| jeez-common | - | 公共模块 |

---

## jeez-common 公共模块规范（重要）

**所有业务模块都依赖 jeez-common**，在重构时必须优先使用 common 中的组件。

### 现有公共组件

```
jeez-common/src/main/java/com/jeez/common/
├── Result.java              # 统一响应结果包装类（必须使用）
├── PageResult.java          # 分页结果包装类（必须使用）
├── Constants.java           # 系统常量定义
├── utils/
│   ├── AvatarGenerator.java # 头像生成工具
│   ├── OkHttpUtil.java      # HTTP请求工具
│   └── PasswordUtil.java    # 密码加密工具
└── mq/
    ├── RocketMQUtil.java    # RocketMQ 消息发送工具（必须使用）
    ├── RocketMQCallback.java # 异步消息回调接口
    └── MQConstants.java     # MQ Topic/Tag/ConsumerGroup 常量
```

### 使用原则（必须遵守）

1. **优先复用**：重构前先查看 common 模块是否已有相关工具类
   - 响应封装：使用 `Result<T>` 和 `PageResult<T>`
   - 密码处理：使用 `PasswordUtil`
   - HTTP请求：使用 `OkHttpUtil`
   - 头像生成：使用 `AvatarGenerator`
   - 消息队列：使用 `RocketMQUtil` 和 `MQConstants`

2. **提取通用代码到 common**：
   - 如果发现多个模块有重复的工具类/方法，**必须**提取到 common
   - 新增的通用工具类**必须**放到 `com.jeez.common.utils` 包下
   - 新增的通用常量**必须**放到 `Constants.java` 或新建常量类

3. **禁止放入 common 的内容**：
   - ❌ 业务实体类（Entity）
   - ❌ 业务相关的 DTO/Request/Response
   - ❌ 特定模块的业务逻辑
   - ❌ 只有单个模块使用的工具类

4. **适合放入 common 的内容**：
   - ✅ 通用工具类（日期处理、字符串处理、加密等）
   - ✅ 通用异常类（可考虑将 BusinessException 移至 common）
   - ✅ 通用常量
   - ✅ 通用注解
   - ✅ 通用配置基类
   - ✅ 响应封装类（Result、PageResult）

### 发现重复代码时的处理流程

1. **识别重复**：在重构过程中发现多个模块有相似代码
2. **评估通用性**：判断该代码是否具有通用价值
3. **提取到 common**：如果通用，则提取到 jeez-common 模块
4. **更新引用**：修改各业务模块，改为引用 common 中的实现
5. **删除重复**：删除业务模块中的重复代码

### 常见可提取的代码示例

```java
// 如果多个模块都有类似的日期工具类，应提取到 common
package com.jeez.common.utils;

public class DateUtil {
    public static String formatDateTime(LocalDateTime dateTime) { ... }
    public static LocalDateTime parseDateTime(String str) { ... }
}

// 如果多个模块都有类似的字符串工具类，应提取到 common
package com.jeez.common.utils;

public class StringUtil {
    public static boolean isEmpty(String str) { ... }
    public static String generateCode(int length) { ... }
}
```

---

## 技术栈规范（必须遵守）

### 核心版本
```
Java: 17
Spring Boot: 3.1.5
Spring Cloud: 2022.0.4
MyBatis-Plus: 3.5.9
Sa-Token: 1.37.0
MySQL: 8.0.33
Redis: 最新稳定版
Hutool: 5.8.24
Lombok: 1.18.30
SpringDoc-OpenAPI: 2.3.0
```

### 1. 安全框架 - Sa-Token

**统一配置**（所有业务模块的 application.yml）：
```yaml
sa-token:
  token-name: Authorization
  timeout: 2592000        # 30天
  active-timeout: 1800    # 30分钟
  is-concurrent: true
  is-share: false
  token-style: uuid
  token-prefix: Bearer
  is-log: true
  check-token-sign: false # 信任网关验证
```

**Sa-Token Redis集成**：
```xml
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-redis-jackson</artifactId>
    <version>1.37.0</version>
</dependency>
```

### 2. 数据库 ORM - MyBatis-Plus

**统一使用 MyBatis-Plus**，禁止使用其他 ORM（JPA、原生MyBatis注解）。

**MyBatis-Plus 配置**（所有模块统一）：
```yaml
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.jeez.${module}.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted      # 统一使用 deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

**自动填充配置**（MyBatisPlusConfig.java）：
```java
@Configuration
public class MyBatisPlusConfig implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
    }
}
```

**SQL编写规范**：
- **必须使用** XML 文件管理 SQL 语句
- XML 文件位置：`src/main/resources/mapper/`
- **禁止**在 Mapper 接口中使用 `@Select`、`@Insert` 等注解编写 SQL

### 3. Redis 配置

**统一使用 Redis 0号库**：
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0           # 统一使用0号库
      lettuce:
        pool:
          max-active: 8
          max-wait: -1ms
          max-idle: 8
          min-idle: 0
```

**缓存配置类**（需要缓存的模块添加）：
```java
@Configuration
@EnableCaching
public class RedisCacheConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GenericJackson2JsonRedisSerializer serializer =
            new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        return template;
    }
}
```

### 4. Jackson 配置

**统一日期时间格式**（所有模块）：
```yaml
spring:
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
    serialization:
      write-dates-as-timestamps: false
```

### 5. 日志配置

**统一日志配置**：
```yaml
logging:
  level:
    root: info
    com.jeez: debug
    com.baomidou.mybatisplus: debug
    cn.dev33.satoken: debug
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{50} - %msg%n"
```

### 6. 中间件配置

#### 6.1 对象存储 - MinIO / RustFS

项目支持两种对象存储方案，通过配置切换：

**MinIO 配置**：
```yaml
image:
  storage:
    type: minio  # 或 rustfs

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin123
  bucket-name: jeez-fitness
```

**RustFS 配置**：
```yaml
image:
  storage:
    type: rustfs

rustfs:
  server:
    url: http://localhost:8000
  access-key: your-access-key
  secret-key: your-secret-key
```

**使用方式**：注入 `ImageService` 接口即可，会自动根据配置选择实现。

#### 6.2 消息队列 - RocketMQ

**RocketMQ 配置**：
```yaml
rocketmq:
  name-server: localhost:9876
  producer:
    group: jeez-producer-group
    send-message-timeout: 3000
    retry-times-when-send-failed: 2
    retry-times-when-send-async-failed: 2
```

**使用方式**：注入 `RocketMQUtil`（位于 jeez-common）发送消息：

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final RocketMQUtil rocketMQUtil;

    public void createOrder(Order order) {
        // 业务逻辑...

        // 发送消息
        rocketMQUtil.syncSend(MQConstants.TOPIC_ORDER, order);

        // 带 Tag 发送
        rocketMQUtil.syncSend(MQConstants.TOPIC_ORDER, MQConstants.OrderTag.CREATE, order);

        // 延迟消息（30分钟后发送）
        rocketMQUtil.syncSendDelay(MQConstants.TOPIC_ORDER, order, MQConstants.DelayLevel.MINUTE_30);
    }
}
```

**消费者示例**：
```java
@Component
@RocketMQMessageListener(
    topic = MQConstants.TOPIC_ORDER,
    consumerGroup = MQConstants.ConsumerGroup.ORDER_GROUP
)
public class OrderConsumer implements RocketMQListener<Order> {
    @Override
    public void onMessage(Order order) {
        // 处理消息
    }
}
```

**MQ 常量**：所有 Topic、Tag、ConsumerGroup 统一在 `MQConstants` 中定义。

---

## 包结构规范（必须遵守）

```
com.jeez.{module}/
├── config/                 # 配置类
│   ├── MyBatisPlusConfig.java
│   ├── SwaggerConfig.java
│   └── RedisCacheConfig.java (可选)
├── controller/             # 控制器
├── service/                # 服务接口
│   └── impl/               # 服务实现（小写 impl）
├── mapper/                 # Mapper 接口
├── entity/                 # 实体类
├── dto/                    # 数据传输对象
│   ├── request/            # 请求对象
│   └── response/           # 响应对象
├── exception/              # 异常类
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
├── constants/              # 常量类（可选）
└── utils/                  # 工具类（可选）
```

**禁止的包结构**：
- `service/Impl`（大写I）
- `entity/dto`（DTO不应在entity下）
- `vo/`（统一使用dto/response）
- `query/`（统一使用dto/request）

---

## 命名规范（必须遵守）

### 1. DTO 命名规范

| 类型 | 命名格式 | 示例 |
|------|---------|------|
| 创建请求 | `{Entity}CreateRequest` | `UserCreateRequest` |
| 更新请求 | `{Entity}UpdateRequest` | `UserUpdateRequest` |
| 查询请求 | `{Entity}QueryRequest` | `UserQueryRequest` |
| 响应对象 | `{Entity}Response` | `UserResponse` |
| 列表响应 | `{Entity}ListResponse` | `UserListResponse` |

**禁止的命名**：
- `*VO`（统一使用 `*Response`）
- `*DTO` 作为请求/响应对象（DTO 仅用于内部传输）
- 模糊命名如 `UserInfo`、`UserData`

### 2. 实体类命名

**字段命名统一**：
```java
@Data
@TableName("table_name")
public class Entity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;    // 统一 createTime

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;    // 统一 updateTime

    @TableLogic
    private Integer deleted;             // 统一 deleted（不是 is_deleted）
}
```

### 3. Service 命名

```java
// 接口
public interface UserService { }

// 实现类（在 service/impl 包下）
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService { }
```

### 4. Controller 命名

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController { }
```

---

## 异常处理规范（必须遵守）

### 1. 统一异常类

```java
package com.jeez.{module}.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;  // 统一使用 int 类型

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
```

### 2. 统一异常处理器

```java
package com.jeez.{module}.exception;

import cn.dev33.satoken.exception.*;
import com.jeez.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        log.warn("未登录: {}", e.getMessage());
        return Result.error(401, "未登录或登录已过期");
    }

    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleNotPermissionException(NotPermissionException e) {
        log.warn("无权限: {}", e.getMessage());
        return Result.error(403, "无权限访问");
    }

    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleNotRoleException(NotRoleException e) {
        log.warn("角色不足: {}", e.getMessage());
        return Result.error(403, "角色权限不足");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("参数校验失败");
        return Result.error(400, message);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("参数绑定失败");
        return Result.error(400, message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(500, "系统内部错误");
    }
}
```

---

## API 文档规范（Swagger/SpringDoc）

### Controller 注解规范

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    @Operation(summary = "创建用户", description = "创建新用户账号")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "401", description = "未登录")
    })
    @PostMapping
    public Result<UserResponse> createUser(@RequestBody @Valid UserCreateRequest request) {
        // ...
    }
}
```

### DTO 注解规范

```java
@Data
@Schema(description = "用户创建请求")
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "john_doe", required = true)
    private String username;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "john@example.com")
    private String email;
}
```

---

## 依赖注入规范

**统一使用构造器注入**：
```java
@Service
@RequiredArgsConstructor  // Lombok 自动生成构造器
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;        // final 必须
    private final RedisTemplate<String, Object> redisTemplate;

    // 不要使用 @Autowired
}
```

---

## 禁止事项

1. **禁止**模块间直接依赖（如 store 依赖 auth），应通过网关调用
2. **禁止**使用 `@Autowired` 字段注入
3. **禁止**在 Mapper 接口中使用注解 SQL
4. **禁止**使用 `is_deleted` 字段名，统一使用 `deleted`
5. **禁止**使用 `*VO` 命名，统一使用 `*Response`
6. **禁止**在 entity 包下创建 dto 子包
7. **禁止**使用大写 `Impl` 包名，统一使用小写 `impl`
8. **禁止** BusinessException 的 code 字段使用 String 类型

---

## 重构执行流程

当收到重构任务时，按以下步骤执行：

### 第零步：创建 refactor 分支
1. 使用 AskUserQuestion 询问用户：
   - 重构范围（模块名或描述）
   - 涉及的模块
2. 执行：
   ```bash
   git checkout develop && git pull
   git checkout -b refactor/模块名或描述
   ```

### 第一步：查看数据库结构和 common 模块（必须首先执行）
1. 阅读 `E:\Code\Jeez.Fitness\db\dump-jeez_fitness-202512120956.sql` 了解表结构
2. 查看 `jeez-common/src/main/java/com/jeez/common/` 下已有的工具类
3. 记录可复用的组件，避免重复造轮子

### 第二步：分析当前代码
1. 阅读目标模块的现有代码
2. 识别业务逻辑（保留）
3. 识别技术实现（重写）

### 第三步：重构包结构
1. 调整包结构符合规范
2. 移动类到正确位置
3. 修正包名大小写

### 第四步：重构命名
1. 重命名 DTO/VO 类符合规范
2. 统一字段命名
3. 更新所有引用

### 第五步：重构配置
1. 统一 application.yml 配置
2. 添加/更新配置类
3. 确保 Redis 使用 0 号库

### 第六步：重构异常处理
1. 统一 BusinessException 实现
2. 添加/更新 GlobalExceptionHandler
3. 确保 code 字段为 int 类型

### 第七步：重构 ORM
1. 确保使用 MyBatis-Plus
2. SQL 移至 XML 文件
3. 统一逻辑删除字段为 deleted

### 第八步：添加文档注解
1. Controller 添加 @Tag, @Operation
2. DTO 添加 @Schema
3. 确保 Swagger 文档完整

### 第九步：整合公共代码到 common
1. 检查是否有重复的工具类/方法
2. 将通用代码提取到 `jeez-common` 模块
3. 更新各模块引用，使用 common 中的实现
4. 删除模块中的重复代码

### 第十步：编译验证
1. 使用 TodoWrite 跟踪重构进度
2. 如果修改了 common: `mvn clean install -DskipTests -pl jeez-common`
3. 编译所有相关模块: `mvn clean compile -DskipTests -pl 模块名`
4. 修复所有编译错误

### 第十一步：提交并合并
1. 提交到 refactor 分支:
   ```bash
   git add .
   git commit -m "refactor(模块): 重构描述

   🤖 Generated with [Claude Code](https://claude.com/claude-code)

   Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>"
   git push origin refactor/xxx
   ```
2. 合并到 develop:
   ```bash
   git checkout develop
   git pull
   git merge refactor/xxx --no-ff -m "refactor(模块): 合并重构"
   git push origin develop
   ```
3. 输出完成信息（重构范围、修改的文件数、统一的规范）

---

## 当前已知的不一致问题

### 需要修复的模块问题：

1. **jeez-store-fitness**
   - 逻辑删除字段使用 `is_deleted`，需改为 `deleted`
   - 依赖了 jeez-auth-fitness，需移除

2. **jeez-coach-fitness**
   - 使用 `*VO` 命名，需改为 `*Response`
   - 使用 `service/Impl`（大写I），需改为 `service/impl`
   - 有 `query/` 包，需合并到 `dto/request/`

3. **jeez-equipment-fitness**
   - DTO 放在 `entity/dto` 下，需移到 `dto/` 包
   - 使用了 fastjson 1.2.83，建议移除或升级

4. **jeez-manager-fitness**
   - 依赖了 jeez-auth-fitness，需移除
   - GlobalExceptionHandler 功能不完整，需补充

5. **多个模块的 BusinessException**
   - code 字段类型不统一（int vs String）
   - 需要统一为 int 类型

---

现在请告诉我你要重构哪个模块，或者需要执行什么具体的重构任务。
