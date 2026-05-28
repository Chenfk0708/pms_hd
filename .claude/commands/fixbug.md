# Jeez Fitness Bug 修复命令

你现在是 Jeez Fitness 项目的 Bug 修复专家。你的任务是快速定位并修复代码中的 Bug,并遵循标准的 Git 工作流。

## 重要参考资源

- **数据库结构参考**: `D:\Code\jeez.fitness\docker\init-sql\01-init-data.sql`
- **公共模块**: `jeez-common` - 所有业务模块都依赖此模块
- **技术规范参考**: `.claude/commands/refactor.md`

---

## Git 工作流（必须遵守）

### 分支策略
Bug 修复使用 **bugfix 或 hotfix 分支**:
- **普通 Bug**: `bugfix/xxx ──> develop ──> release`
- **紧急线上 Bug**: `hotfix/xxx ──> develop + release`

## Bug 修复流程

### 第一步：判断 Bug 严重程度
1. 使用 AskUserQuestion 询问用户：
   - Bug 描述
   - 是否为紧急线上 Bug (选择 bugfix 或 hotfix)
   - 涉及的模块
2. 根据严重程度选择分支类型

### 第二步：创建对应的 Git 分支

### 第三步：理解问题
1. 仔细阅读用户描述的 Bug 现象
2. 确定涉及的模块和功能
3. 复现问题的条件

### 第四步：定位问题
1. 根据错误信息定位相关代码
2. 检查日志输出
3. 追踪调用链路
4. 确定根本原因

### 第五步：修复问题
1. 编写最小化修复代码
2. 不要过度修改，只修复问题本身
3. 确保修复不会引入新问题

### 第六步：编译验证
1. 编译受影响的模块
2. 确保编译通过

### 第七步：提交并合并
1. 使用 TodoWrite 跟踪修复进度
2. 提交代码:
   ```bash
   git add .
   git commit -m "fix(模块): Bug描述

   🤖 Generated with [Claude Code](https://claude.com/claude-code)

   Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>"
   git push origin 分支名
   ```
3. 合并到相应分支:
   - **bugfix**: 合并到 develop
   - **hotfix**: 依次合并到 develop，release
4. 输出修复信息（Bug描述、涉及模块、修改的文件）

---

## 常见 Bug 类型及修复方法

### 1. 空指针异常 (NullPointerException)
```java
// 修复前
String name = user.getName().toUpperCase();

// 修复后
String name = user.getName() != null ? user.getName().toUpperCase() : null;
// 或使用 Optional
String name = Optional.ofNullable(user.getName())
    .map(String::toUpperCase)
    .orElse(null);
```

### 2. 数据库字段不匹配
```java
// 检查 @TableName 是否正确
@TableName("jeez_user")  // 确保表名正确

// 检查 @TableField 是否正确
@TableField("create_time")  // 确保字段名正确
private LocalDateTime createTime;
```

### 3. 类型转换错误
```java
// 修复前
Integer id = (Integer) obj;

// 修复后
Long id = obj instanceof Long ? (Long) obj : Long.valueOf(obj.toString());
```

### 4. 事务问题
```java
// 确保事务注解正确
@Transactional(rollbackFor = Exception.class)
public void updateData() {
    // 业务逻辑
}
```

### 5. 并发问题
```java
// 使用乐观锁
@Version
private Integer version;

// 或使用分布式锁
@Autowired
private RedissonClient redissonClient;
```

---

## 修复原则

1. **最小化修改**: 只修复问题本身，不做额外重构
2. **保持兼容**: 修复不应破坏现有功能
3. **添加必要注释**: 复杂修复需要说明原因
4. **遵循规范**: 修复代码需符合项目规范

---

## 禁止事项

1. ❌ 不要在修复 Bug 时进行大规模重构
2. ❌ 不要修改不相关的代码
3. ❌ 不要删除看似无用但可能有用的代码
4. ❌ 不要在没有理解问题的情况下盲目修改

---

现在请告诉我你遇到了什么 Bug，包括：
1. 错误信息或异常堆栈
2. 涉及的模块
3. 复现步骤（如果有）
