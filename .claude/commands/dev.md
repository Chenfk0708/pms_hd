# Jeez Fitness 业务开发命令

你现在是 Jeez Fitness 项目的业务开发专家。你的任务是按照统一的技术规范编写高质量的业务代码,并遵循标准的 Git 工作流。

## 重要参考资源

- **数据库结构参考**: `D:\Code\jeez.fitness\docker\init-sql\01-init-data.sql`
- **公共模块**: `jeez-common` - 所有业务模块都依赖此模块
- **技术规范参考**: `.claude/commands/refactor.md`

---

## Git 工作流（必须遵守）

### 分支策略
开发新功能使用 **feature 分支**:
```
feature/xxx ──> develop ──> release ──> production
```

### 执行流程
1. **从 develop 创建 feature 分支**
   ```bash
   git checkout develop
   git pull
   git checkout -b feature/功能名称
   ```

2. **开发完成后提交代码**
   ```bash
   git add .
   git commit -m "feat(模块): 功能描述"
   git push origin feature/功能名称
   ```

3. **合并到 develop**
   ```bash
   git checkout develop
   git pull
   git merge feature/功能名称 --no-ff
   ```

4. **编译验证受影响的模块**
   - 如果修改了 common: `mvn clean install -DskipTests -pl jeez-common`
   - 编译业务模块: `mvn clean compile -DskipTests -pl 模块名`

5. **推送 develop**
   ```bash
   git push origin develop
   ```

---

## 项目模块

| 模块 | 端口 | 职责 |
|------|------|------|
| jeez-gateway | 8080 | API网关，路由转发 |
| jeez-auth-fitness | 8081 | 认证服务，用户管理 |
| jeez-member-fitness | 8082 | 会员服务 |
| jeez-store-fitness | 8083 | 门店服务 |
| jeez-equipment-fitness | 8084 | 设备服务 |
| jeez-coach-fitness | 8085 | 教练服务 |
| jeez-course-fitness | 8086 | 课程服务 |
| jeez-manager-fitness | 8087 | 管理服务 |
| jeez-order-fitness | 8088 | 订单支付服务 |
| jeez-common | - | 公共模块 |

---

## jeez-common 公共模块（必须优先使用）

### 现有公共组件

```
jeez-common/src/main/java/com/jeez/common/
├── Result.java              # 统一响应封装（必须使用）
├── PageResult.java          # 分页结果封装（必须使用）
├── Constants.java           # 系统常量
├── utils/
│   ├── AvatarGenerator.java # 头像生成工具
│   ├── OkHttpUtil.java      # HTTP请求工具
│   └── PasswordUtil.java    # 密码加密工具
└── mq/
    ├── RocketMQUtil.java    # RocketMQ 消息发送工具（必须使用）
    ├── RocketMQCallback.java # 异步消息回调接口
    └── MQConstants.java     # MQ Topic/Tag/ConsumerGroup 常量
```

### 使用原则

1. **开发前必须先查看 common 模块**，看是否已有可复用的组件
2. **响应必须使用** `Result<T>` 和 `PageResult<T>`
3. **消息队列必须使用** `RocketMQUtil` 和 `MQConstants`
4. **新增通用工具类必须放到 common**，而非业务模块
5. **禁止在 common 放业务实体和 DTO**

---

## 技术栈规范

### 核心版本
```
Java: 17
Spring Boot: 3.1.5
MyBatis-Plus: 3.5.9
Sa-Token: 1.37.0
Redis: 0号库
SpringDoc-OpenAPI: 2.3.0
```

### 安全框架 - Sa-Token
```yaml
sa-token:
  token-name: Authorization
  timeout: 2592000
  active-timeout: 1800
  is-concurrent: true
  is-share: false
  token-style: uuid
  token-prefix: Bearer
  check-token-sign: false
```

### 数据库 ORM - MyBatis-Plus
- **必须使用** MyBatis-Plus
- **必须使用** XML 文件编写 SQL（位于 `src/main/resources/mapper/`）
- **禁止**使用 `@Select`、`@Insert` 等注解 SQL

### Redis 配置
- **统一使用 0 号库**
- 使用 Lettuce 连接池

### 中间件配置

#### 对象存储 - MinIO / RustFS

项目支持两种对象存储，通过配置 `image.storage.type` 切换：

```yaml
# MinIO 配置
image:
  storage:
    type: minio

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin123
  bucket-name: jeez-fitness
```

```yaml
# RustFS 配置
image:
  storage:
    type: rustfs

rustfs:
  server:
    url: http://localhost:8000
  access-key: your-access-key
  secret-key: your-secret-key
```

使用方式：注入 `ImageService` 接口。

#### 消息队列 - RocketMQ

```yaml
rocketmq:
  name-server: localhost:9876
  producer:
    group: jeez-producer-group
    send-message-timeout: 3000
```

使用方式：注入 `RocketMQUtil`（来自 jeez-common）。

---

## 包结构规范

```
com.jeez.{module}/
├── config/                 # 配置类
├── controller/             # 控制器
├── service/                # 服务接口
│   └── impl/               # 服务实现（小写 impl）
├── mapper/                 # Mapper 接口
├── entity/                 # 实体类
├── dto/                    # 数据传输对象
│   ├── request/            # 请求对象
│   └── response/           # 响应对象
├── exception/              # 异常类
├── constants/              # 常量类（可选）
└── utils/                  # 工具类（可选，优先放 common）
```

---

## 命名规范

### DTO 命名
| 类型 | 命名格式 | 示例 |
|------|---------|------|
| 创建请求 | `{Entity}CreateRequest` | `MemberCreateRequest` |
| 更新请求 | `{Entity}UpdateRequest` | `MemberUpdateRequest` |
| 查询请求 | `{Entity}QueryRequest` | `MemberQueryRequest` |
| 响应对象 | `{Entity}Response` | `MemberResponse` |

**禁止**：`*VO`、`*DTO`（作为请求/响应）、`*Info`、`*Data`

### 实体类字段
```java
@Data
@TableName("table_name")
public class Entity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;  // 统一使用 deleted
}
```

---

## 代码模板

### Controller
```java
@RestController
@RequestMapping("/api/v1/{resources}")
@RequiredArgsConstructor
@Tag(name = "模块名称", description = "模块描述")
public class XxxController {

    private final XxxService xxxService;

    @Operation(summary = "创建xxx")
    @PostMapping
    public Result<XxxResponse> create(@RequestBody @Valid XxxCreateRequest request) {
        return Result.success(xxxService.create(request));
    }

    @Operation(summary = "根据ID查询")
    @GetMapping("/{id}")
    public Result<XxxResponse> getById(@PathVariable Long id) {
        return Result.success(xxxService.getById(id));
    }

    @Operation(summary = "分页查询")
    @GetMapping
    public Result<PageResult<XxxResponse>> page(XxxQueryRequest request) {
        return Result.success(xxxService.page(request));
    }

    @Operation(summary = "更新xxx")
    @PutMapping("/{id}")
    public Result<XxxResponse> update(@PathVariable Long id,
                                       @RequestBody @Valid XxxUpdateRequest request) {
        return Result.success(xxxService.update(id, request));
    }

    @Operation(summary = "删除xxx")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        xxxService.delete(id);
        return Result.success(null);
    }
}
```

### Service 接口
```java
public interface XxxService {
    XxxResponse create(XxxCreateRequest request);
    XxxResponse getById(Long id);
    PageResult<XxxResponse> page(XxxQueryRequest request);
    XxxResponse update(Long id, XxxUpdateRequest request);
    void delete(Long id);
}
```

### Service 实现
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class XxxServiceImpl implements XxxService {

    private final XxxMapper xxxMapper;

    @Override
    public XxxResponse create(XxxCreateRequest request) {
        Xxx entity = new Xxx();
        BeanUtils.copyProperties(request, entity);
        xxxMapper.insert(entity);
        return convertToResponse(entity);
    }

    @Override
    public XxxResponse getById(Long id) {
        Xxx entity = xxxMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("数据不存在");
        }
        return convertToResponse(entity);
    }

    @Override
    public PageResult<XxxResponse> page(XxxQueryRequest request) {
        Page<Xxx> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Xxx> wrapper = new LambdaQueryWrapper<>();
        // 添加查询条件
        xxxMapper.selectPage(page, wrapper);

        List<XxxResponse> records = page.getRecords().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public XxxResponse update(Long id, XxxUpdateRequest request) {
        Xxx entity = xxxMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("数据不存在");
        }
        BeanUtils.copyProperties(request, entity);
        xxxMapper.updateById(entity);
        return convertToResponse(entity);
    }

    @Override
    public void delete(Long id) {
        int rows = xxxMapper.deleteById(id);
        if (rows == 0) {
            throw new BusinessException("删除失败，数据不存在");
        }
    }

    private XxxResponse convertToResponse(Xxx entity) {
        XxxResponse response = new XxxResponse();
        BeanUtils.copyProperties(entity, response);
        return response;
    }
}
```

### Mapper
```java
@Mapper
public interface XxxMapper extends BaseMapper<Xxx> {
    // 复杂查询在 XML 中实现
}
```

### Mapper XML
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.jeez.xxx.mapper.XxxMapper">

    <!-- 复杂查询示例 -->
    <select id="selectByCondition" resultType="com.jeez.xxx.entity.Xxx">
        SELECT * FROM xxx
        WHERE deleted = 0
        <if test="name != null and name != ''">
            AND name LIKE CONCAT('%', #{name}, '%')
        </if>
        ORDER BY create_time DESC
    </select>

</mapper>
```

### Request DTO
```java
@Data
@Schema(description = "创建请求")
public class XxxCreateRequest {

    @NotBlank(message = "名称不能为空")
    @Schema(description = "名称", required = true)
    private String name;

    @Schema(description = "描述")
    private String description;
}
```

### Response DTO
```java
@Data
@Schema(description = "响应对象")
public class XxxResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
```

### 异常类
```java
@Getter
public class BusinessException extends RuntimeException {
    private final int code;

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

### 全局异常处理器
```java
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
        return Result.error(401, "未登录或登录已过期");
    }

    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleNotPermissionException(NotPermissionException e) {
        return Result.error(403, "无权限访问");
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

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(500, "系统内部错误");
    }
}
```

### RocketMQ 消息发送示例
```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final RocketMQUtil rocketMQUtil;

    public void createOrder(Order order) {
        // 业务逻辑...

        // 同步发送
        rocketMQUtil.syncSend(MQConstants.TOPIC_ORDER, order);

        // 带 Tag 发送
        rocketMQUtil.syncSend(MQConstants.TOPIC_ORDER, MQConstants.OrderTag.CREATE, order);

        // 延迟消息（30分钟后）
        rocketMQUtil.syncSendDelay(MQConstants.TOPIC_ORDER, order, MQConstants.DelayLevel.MINUTE_30);
    }
}
```

### RocketMQ 消费者示例
```java
@Slf4j
@Component
@RocketMQMessageListener(
    topic = MQConstants.TOPIC_ORDER,
    consumerGroup = MQConstants.ConsumerGroup.ORDER_GROUP
)
public class OrderConsumer implements RocketMQListener<Order> {

    @Override
    public void onMessage(Order order) {
        log.info("收到订单消息: {}", order);
        // 处理消息逻辑
    }
}
```

---

## 开发流程

### 第一步：创建 feature 分支
1. 使用 AskUserQuestion 询问用户：
   - 功能名称（用于分支名）
   - 涉及的模块
2. 执行：
   ```bash
   git checkout develop && git pull
   git checkout -b feature/功能名称
   ```

### 第二步：准备工作
1. 阅读 `db/dump-jeez_fitness-202512120956.sql` 了解相关表结构
2. 查看 `jeez-common` 是否有可复用的组件
3. 确认要开发的模块

### 第三步：创建实体类
1. 在 `entity/` 包下创建实体类
2. 字段与数据库表对应
3. 使用 `@TableName`、`@TableId`、`@TableField` 等注解

### 第四步：创建 Mapper
1. 在 `mapper/` 包下创建 Mapper 接口
2. 继承 `BaseMapper<Entity>`
3. 在 `resources/mapper/` 下创建 XML 文件

### 第五步：创建 DTO
1. 在 `dto/request/` 下创建请求对象
2. 在 `dto/response/` 下创建响应对象
3. 添加 `@Schema` 和校验注解

### 第六步：创建 Service
1. 在 `service/` 下创建接口
2. 在 `service/impl/` 下创建实现类
3. 使用 `@RequiredArgsConstructor` 注入依赖

### 第七步：创建 Controller
1. 在 `controller/` 下创建控制器
2. 添加 `@Tag`、`@Operation` 等 Swagger 注解
3. 返回值统一使用 `Result<T>`

### 第八步：编译验证
1. 编译相关模块
2. 如果修改了 common: `mvn clean install -DskipTests -pl jeez-common`
3. 编译业务模块: `mvn clean compile -DskipTests -pl 模块名`
4. 修复编译错误

### 第九步：提交并合并
1. 使用 TodoWrite 跟踪进度
2. 提交到 feature 分支:
   ```bash
   git add .
   git commit -m "feat(模块): 功能描述

   🤖 Generated with [Claude Code](https://claude.com/claude-code)

   Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>"
   git push origin feature/功能名称
   ```
3. 合并到 develop:
   ```bash
   git checkout develop
   git pull
   git merge feature/功能名称 --no-ff -m "feat(模块): 合并功能"
   git push origin develop
   ```
4. 输出完成信息（功能名称、涉及模块、创建的接口数量）

---

## 禁止事项

1. ❌ 模块间直接依赖（应通过网关调用）
2. ❌ 使用 `@Autowired` 字段注入
3. ❌ 在 Mapper 中使用注解 SQL
4. ❌ 使用 `is_deleted` 字段名
5. ❌ 使用 `*VO` 命名
6. ❌ 在 entity 包下创建 dto
7. ❌ 使用大写 `Impl` 包名
8. ❌ BusinessException 的 code 使用 String
9. ❌ 在业务模块重复造轮子（应放 common）

---

## 依赖注入规范

```java
// 正确方式
@Service
@RequiredArgsConstructor
public class XxxServiceImpl {
    private final XxxMapper xxxMapper;  // final + 构造器注入
}

// 错误方式
@Service
public class XxxServiceImpl {
    @Autowired  // 禁止
    private XxxMapper xxxMapper;
}
```

---

现在请告诉我你要在哪个模块开发什么功能。
