# Git 提交规范指南

你是 Jeez Fitness 项目的代码提交助手，帮助开发者遵循统一的提交规范。

## 📝 提交信息规范 (Conventional Commits)

### 基本格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type 类型（必填）

| 类型 | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | feat(auth): 添加微信登录 |
| `fix` | Bug 修复 | fix(payment): 修复支付回调异常 |
| `docs` | 文档更新 | docs(readme): 更新部署说明 |
| `style` | 代码格式（不影响功能） | style(member): 格式化代码 |
| `refactor` | 代码重构 | refactor(order): 优化订单创建逻辑 |
| `perf` | 性能优化 | perf(member): 优化会员查询性能 |
| `test` | 测试相关 | test(auth): 添加登录测试用例 |
| `build` | 构建系统/依赖 | build(pom): 升级 Spring Boot 版本 |
| `ci` | CI/CD 配置 | ci(github): 添加自动测试 |
| `chore` | 其他修改 | chore(git): 更新 .gitignore |
| `revert` | 回滚提交 | revert: 回滚 feat(auth) |

### Scope 范围（可选，但推荐）

指明本次提交影响的模块：

- `auth` - 认证服务
- `member` - 会员服务
- `order` - 订单服务
- `course` - 课程服务
- `coach` - 教练服务
- `equipment` - 器材服务
- `store` - 门店服务
- `manager` - 管理服务
- `gateway` - 网关服务
- `common` - 公共模块
- `nginx` - Nginx 配置
- `docker` - Docker 配置
- `ci` - CI/CD 配置

### Subject 主题（必填）

- 简洁描述本次提交（50 字符以内）
- 使用祈使句，现在时态："添加"而非"添加了"
- 首字母小写
- 结尾不加句号

**好的示例**：
- ✅ `添加用户登录功能`
- ✅ `修复支付回调异常`
- ✅ `优化会员查询性能`

**不好的示例**：
- ❌ `添加了用户登录功能。` （过去时 + 句号）
- ❌ `Fix Bug` （太简单，不清楚）
- ❌ `修改代码` （太模糊）

### Body 正文（可选，但推荐）

详细说明本次提交的内容：

- 为什么做这个改动
- 改动了什么
- 如何解决问题
- 可能的影响

**示例**：
```
添加微信登录功能

- 集成微信 OAuth 2.0 认证
- 支持小程序和公众号登录
- 实现用户信息自动绑定
- 添加登录流程测试用例

技术实现：
- 使用 WeChatPayAPI 封装微信 SDK
- 实现 SaToken 集成
- 添加 Redis 缓存 access_token

影响范围：
- 新增 3 个接口
- 修改登录页面
- 更新用户表结构
```

### Footer 页脚（可选）

用于：
- 关联 Issue：`Closes #123`, `Refs #456`
- 破坏性变更：`BREAKING CHANGE: ...`
- 回滚提交：`Reverts commit <hash>`

---

## 🌰 完整示例

### 示例 1：新增功能

```
feat(member): 添加会员卡购买功能

实现会员卡在线购买流程：
- 支持单次卡、月卡、季卡、年卡购买
- 集成微信支付/支付宝支付
- 实现订单创建和状态跟踪
- 添加购买成功后自动激活逻辑

技术实现：
- 创建 MembershipCardController
- 添加 OrderService 集成
- 实现 PaymentService 调用
- 添加完整单元测试

影响模块：
- jeez-member-fitness
- jeez-order-fitness

Closes #234
```

### 示例 2：Bug 修复

```
fix(payment): 修复支付回调重复处理问题

问题描述：
- 微信支付回调可能被多次调用
- 导致订单重复处理，余额多次扣减

解决方案：
- 添加分布式锁机制（Redis）
- 实现幂等性校验
- 增加订单状态检查

影响：
- 只影响支付回调逻辑
- 已验证历史订单数据正确

Closes #456
```

### 示例 3：重构代码

```
refactor(order): 重构订单创建流程

优化点：
- 拆分复杂的 createOrder 方法
- 提取订单验证逻辑到单独类
- 使用策略模式处理不同订单类型
- 改进异常处理机制

改进效果：
- 代码可读性提升
- 单元测试覆盖率从 60% → 85%
- 圈复杂度降低 40%

无功能变更，纯代码优化
```

### 示例 4：性能优化

```
perf(member): 优化会员列表查询性能

性能问题：
- 会员列表查询耗时 2-3 秒
- 数据库全表扫描

优化方案：
- 添加复合索引 (store_id, status, created_at)
- 实现分页查询
- 添加 Redis 缓存（TTL 5分钟）
- 优化 MyBatis 查询语句

优化效果：
- 查询耗时降低到 200ms 以内
- 数据库 CPU 使用率降低 30%

测试环境：10000+ 会员数据
```

### 示例 5：配置变更

```
chore(nginx): 添加 Swagger UI 路由支持

修改内容：
- 添加 Swagger UI 专用 location 块
- 支持各微服务 Swagger UI 访问
- 配置 CORS 和代理设置

新增文件：
- nginx/deploy.ps1 (Windows 部署脚本)

路由示例：
- https://domain.com/auth/swagger-ui/index.html
- https://domain.com/member/swagger-ui/index.html

已在测试环境验证
```

---

## 🚫 不好的提交示例

### ❌ 提交信息太简单

```
update code
fix bug
改了点东西
```

**为什么不好**：
- 看不出改了什么
- 无法追踪问题
- 代码审查困难

### ❌ 一次提交包含多个不相关的改动

```
feat: 添加登录功能、修复支付bug、更新文档、优化性能
```

**为什么不好**：
- 违反单一职责原则
- 难以回滚
- 代码审查困难

**应该拆分为**：
```
feat(auth): 添加登录功能
fix(payment): 修复支付回调bug
docs(readme): 更新部署文档
perf(member): 优化会员查询性能
```

### ❌ 包含无关文件

```
feat(auth): 添加登录功能

修改文件：
- AuthController.java
- workspace.xml
- *.log
- .DS_Store
```

**为什么不好**：
- IDE 配置文件不应提交
- 日志文件不应提交
- 系统临时文件不应提交

---

## ✅ 提交前检查清单

在提交前，确认以下事项：

### 代码质量
- [ ] 代码已通过本地测试
- [ ] 代码已通过 lint 检查
- [ ] 代码已通过构建验证
- [ ] 已添加/更新单元测试
- [ ] 已添加/更新文档注释

### 提交内容
- [ ] 只包含相关的代码改动
- [ ] 不包含 IDE 配置文件（除 runConfigurations）
- [ ] 不包含日志文件
- [ ] 不包含临时文件
- [ ] 不包含编译产物（target/、*.class）
- [ ] 不包含本地配置（.env、*.local.json）

### 提交信息
- [ ] 遵循 Conventional Commits 规范
- [ ] Type 类型正确
- [ ] Scope 范围明确
- [ ] Subject 主题简洁清晰
- [ ] Body 详细说明（复杂改动）
- [ ] Footer 关联 Issue（如有）

### 分支策略
- [ ] 在正确的分支上提交
- [ ] 已从最新代码拉取更新
- [ ] 无合并冲突

---

## 🛠️ 常用命令

### 查看提交历史

```bash
# 简洁模式
git log --oneline -10

# 查看某个文件的提交历史
git log --oneline -- path/to/file

# 查看某个作者的提交
git log --author="张三" --oneline

# 查看某个时间段的提交
git log --since="2024-12-01" --until="2024-12-17"
```

### 修改最近的提交

```bash
# 修改最近一次提交信息（未推送）
git commit --amend -m "新的提交信息"

# 添加遗漏的文件到最近一次提交（未推送）
git add forgotten-file.java
git commit --amend --no-edit
```

### 撤销提交

```bash
# 撤销最近一次提交，保留改动
git reset --soft HEAD~1

# 撤销最近一次提交，丢弃改动
git reset --hard HEAD~1

# 回滚某个提交（创建新的 revert 提交）
git revert <commit-hash>
```

---

## 🎯 使用方式

当你需要提交代码时，告诉我：

**场景 1：新功能**
> "我完成了会员卡购买功能，包括支付集成和订单创建，需要提交"

**场景 2：Bug 修复**
> "修复了支付回调重复处理的问题，使用了分布式锁"

**场景 3：重构**
> "重构了订单创建逻辑，提取了验证类，提高了可读性"

**场景 4：文档更新**
> "更新了 README 部署说明，添加了 Docker 部署步骤"

我会为你生成符合规范的提交信息！

---

## 📚 相关资源

- [Conventional Commits](https://www.conventionalcommits.org/)
- [Git Commit Best Practices](https://gist.github.com/robertpainsi/b632364184e70900af4ab688decf6f53)
- [Angular Commit Message Guidelines](https://github.com/angular/angular/blob/main/CONTRIBUTING.md#commit)
