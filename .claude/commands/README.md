# Jeez Fitness Claude 命令汇总

## 📋 可用命令列表

| 命令 | 用途 | Git 工作流 |
|------|------|-----------|
| `/dev` | 新功能开发 | ✅ feature 分支 |
| `/refactor` | 代码重构 | ✅ refactor 分支 |
| `/fixbug` | Bug 修复 | ✅ bugfix/hotfix 分支 |
| `/fixDatabase` | 数据库对齐修复 | ✅ fix 分支 |
| `/deploy` | 构建并发布到测试环境 | ✅ 自动合并 develop→release |
| `/merge` | Git 分支合并指南 | - |
| `/commit-guide` | 提交规范指南 | - |

## 🌳 标准 Git 工作流

```
feature/xxx ──┐
refactor/xxx ─┤
bugfix/xxx ───┼──> develop ──> release ──> production
hotfix/xxx ───┘         ↑                      ↓
                        └──────────────────────┘
```

## 🎯 快速开始

### 1. 开发新功能
```bash
/dev
```
自动执行：
1. 从 develop 创建 feature 分支
2. 开发完成后自动提交
3. 合并到 develop
4. 编译验证

### 2. 重构代码
```bash
/refactor
```
自动执行：
1. 从 develop 创建 refactor 分支
2. 重构完成后自动提交
3. 合并到 develop
4. 编译验证

### 3. 修复 Bug
```bash
/fixbug
```
自动执行：
1. 根据严重程度创建 bugfix 或 hotfix 分支
2. 修复完成后自动提交
3. 合并到相应分支
4. 编译验证

### 4. 发布到测试环境
```bash
/deploy
```
自动执行：
1. 合并当前代码到 develop
2. 编译所有 10 个微服务
3. 全部成功后推送到 release

### 5. 查看 Git 工作流指南
```bash
/merge
```
查看完整的分支合并、版本发布、回滚等操作指南

### 6. 查看提交规范
```bash
/commit-guide
```
查看 Conventional Commits 提交规范

## 📚 详细说明

### /dev - 新功能开发

**适用场景**：
- 实现新的业务功能
- 添加新的 API 接口
- 开发新模块

**工作流**：
```
1. 创建 feature 分支 (如: feature/member-card)
2. 按照技术规范开发代码
3. 自动提交并推送
4. 合并到 develop
5. 编译验证所有受影响的模块
```

---

### /refactor - 代码重构

**适用场景**：
- 优化代码结构
- 统一技术实现
- 提取公共代码
- 改进性能

**工作流**：
```
1. 创建 refactor 分支 (如: refactor/order-service)
2. 重构代码（保留业务逻辑）
3. 自动提交并推送
4. 合并到 develop
5. 全面编译验证
```

---

### /fixbug - Bug 修复

**适用场景**：
- 修复普通 Bug → bugfix 分支
- 修复紧急线上 Bug → hotfix 分支

**工作流（普通 Bug）**：
```
1. 创建 bugfix 分支 (如: bugfix/payment-callback)
2. 修复问题
3. 自动提交并推送
4. 合并到 develop
5. 编译验证
```

**工作流（紧急 Bug）**：
```
1. 从 production 创建 hotfix 分支
2. 快速修复
3. 合并到 production, release, develop
4. 创建紧急版本 tag
```

---

### /deploy - 构建并发布

**适用场景**：
- 日常开发完成，需要部署到测试环境
- 多个功能合并后的集成测试

**执行流程**：
```
1. 询问合并内容（当前代码/当前分支/指定分支）
2. 合并到 develop
3. 按顺序编译 10 个微服务：
   - jeez-common (install)
   - jeez-gateway
   - jeez-auth-fitness
   - jeez-member-fitness
   - jeez-coach-fitness
   - jeez-course-fitness
   - jeez-order-fitness
   - jeez-store-fitness
   - jeez-equipment-fitness
   - jeez-manager-fitness
4. 全部成功后推送 develop
5. 合并 develop 到 release
6. 推送 release（触发 CI/CD）
```

**特点**：
- 任何模块编译失败会立即停止
- 不会推送到 release（保护测试环境）
- 详细的编译进度跟踪

---

### /merge - Git 工作流指南

**包含内容**：
- 完整的 GitFlow 分支策略
- 功能开发 → develop 合并
- develop → release 发布
- release → production 上线
- hotfix 紧急修复流程
- 版本回滚流程

---

### /commit-guide - 提交规范

**包含内容**：
- Conventional Commits 规范
- 提交类型说明 (feat/fix/refactor/etc)
- 提交信息模板
- 好的/坏的提交示例
- 提交前检查清单

---

## 🔧 项目技术栈

- **Java**: 17
- **Spring Boot**: 3.1.5
- **MyBatis-Plus**: 3.5.9
- **Sa-Token**: 1.37.0
- **Redis**: 0 号库
- **RocketMQ**: 消息队列
- **SpringDoc**: OpenAPI 文档

## 📦 微服务模块

| 模块 | 端口 | 说明 |
|------|------|------|
| jeez-common | - | 公共模块 |
| jeez-gateway | 8080 | API 网关 |
| jeez-auth-fitness | 8081 | 认证服务 |
| jeez-member-fitness | 8082 | 会员服务 |
| jeez-store-fitness | 8083 | 门店服务 |
| jeez-equipment-fitness | 8084 | 器材服务 |
| jeez-coach-fitness | 8085 | 教练服务 |
| jeez-course-fitness | 8086 | 课程服务 |
| jeez-manager-fitness | 8087 | 管理服务 |
| jeez-order-fitness | 8088 | 订单服务 |

## 🎯 使用建议

1. **日常开发**：使用 `/dev` 开发新功能
2. **代码优化**：使用 `/refactor` 重构代码
3. **问题修复**：使用 `/fixbug` 修复问题
4. **测试发布**：使用 `/deploy` 发布到测试环境
5. **Git 操作**：参考 `/merge` 指南
6. **提交代码**：遵循 `/commit-guide` 规范

## ⚠️ 注意事项

1. 所有命令都会自动执行 Git 操作，确保代码已保存
2. 编译失败会自动停止，不会推送到远程
3. 遵循项目技术规范，代码会自动检查
4. 提交信息会自动生成，符合规范

## 📞 获取帮助

如果命令执行出错或有疑问：
1. 查看命令详细文档：`cat .claude/commands/[命令名].md`
2. 检查 Git 状态：`git status`
3. 查看编译日志：检查错误输出
