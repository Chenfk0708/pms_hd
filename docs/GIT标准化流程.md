# Jeez.Fitness Git 工作流程规范

## 分支说明

| 分支 | 用途 | 权限 |
|------|------|------|
| `master` | 生产环境代码，稳定版本 | 仅管理员可合并 |
| `develop` | 开发集成分支，测试用 | 仅管理员可合并 |
| `feature/*` | 功能开发分支 | 开发者自行管理 |
| `hotfix/*` | 紧急修复分支 | 从master创建 |

---

## 开发者流程

### 1. 开始新功能

```bash
# 确保本地develop是最新的
git checkout develop
git pull origin develop

# 创建功能分支 (命名规范: feature/模块-功能)
git checkout -b feature/member-payment
```

### 2. 日常开发

```bash
# 开发过程中频繁提交
git add .
git commit -m "feat(member): 添加购卡支付功能"

# 定期同步develop的最新代码 (使用rebase保持历史线性)
git fetch origin
git rebase origin/develop
```

### 3. 提交合并请求前

```bash
# 1. 确保代码是最新的
git fetch origin
git rebase origin/develop

# 2. 如果有冲突，解决后继续
git add .
git rebase --continue

# 3. 推送到远程 (如果之前推送过，需要force push)
git push origin feature/member-payment
# 或者
git push origin feature/member-payment --force-with-lease
```

### 4. 创建 Pull Request

在 Gitee 上创建合并请求:
- 源分支: `feature/xxx`
- 目标分支: `develop`
- 填写清晰的描述说明改动内容

---

## 管理员合并流程

### 第一步：检查队员分支状态

```bash
# 更新所有远程分支信息
git fetch origin

# 查看队员分支有哪些提交
git log origin/develop..origin/feature/xxx --oneline

# 查看具体改动了哪些文件
git diff origin/develop..origin/feature/xxx --stat

# 检查有没有敏感文件 (配置、密钥等)
git diff origin/develop..origin/feature/xxx --name-only | grep -E '\.(env|pem|key|json)$'

# 查看分支图，检查是否基于最新 develop
git log --oneline --graph origin/develop origin/feature/xxx -15
```

### 第二步：选择合并方式

#### 方式一: Gitee 网页合并 (推荐)

1. 让队员在 Gitee 创建 PR: `feature/xxx` → `develop`
2. 进入 Pull Request 页面审查代码
3. 选择 **Squash Merge** 合并方式
4. 编写规范的提交信息
5. 确认合并
6. 勾选"合并后删除源分支"

#### 方式二: 命令行合并 (队员已规范操作)

```bash
# 1. 切换到 develop 并确保最新
git checkout develop
git pull origin develop

# 2. 使用 squash merge (把所有提交压缩成一个)
git merge --squash origin/feature/xxx

# 3. 查看将要提交的内容
git status
git diff --cached --stat

# 4. 提交，使用规范的提交信息
git commit -m "feat(store): 添加门店公告管理功能

- 添加公告CRUD接口
- 添加公告列表分页查询
- 添加公告状态管理"

# 5. 推送
git push origin develop

# 6. 删除已合并的分支
git push origin --delete feature/xxx
```

#### 方式三: 命令行合并 (队员未规范操作，需要先 rebase)

当队员没有基于最新 develop 开发，或者有很多杂乱的 merge 提交时使用：

```bash
# 1. 更新本地
git checkout develop
git pull origin develop

# 2. 拉取队员分支到本地 (使用临时分支名)
git fetch origin feature/xxx:feature/xxx-local

# 3. 切换到该分支
git checkout feature/xxx-local

# 4. rebase 到 develop
git rebase develop

# 5. 如果有冲突，解决后继续
#    编辑冲突文件...
git add .
git rebase --continue
#    如果想放弃: git rebase --abort

# 6. rebase 完成后，切回 develop 进行 squash merge
git checkout develop
git merge --squash feature/xxx-local

# 7. 提交并推送
git commit -m "feat(xxx): 功能描述"
git push origin develop

# 8. 清理分支
git branch -D feature/xxx-local
git push origin --delete feature/xxx
```

### 合并前检查清单

| 检查项 | 命令 | 说明 |
|--------|------|------|
| 提交内容 | `git log origin/develop..origin/feature/xxx --oneline` | 查看有哪些提交 |
| 改动文件 | `git diff origin/develop..origin/feature/xxx --stat` | 确认改动范围 |
| 敏感文件 | `git diff ... --name-only \| grep -E '\.(env\|pem)'` | 检查有无敏感信息 |
| 分支状态 | `git log --graph origin/develop origin/feature/xxx -10` | 检查分支是否干净 |

### 发布到生产

```bash
# 从develop合并到master
git checkout master
git pull origin master
git merge develop
git push origin master

# 打标签
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

---

## 提交信息规范

### 格式

```
<类型>(<模块>): <简短描述>

[可选的详细描述]

[可选的关联Issue]
```

### 类型说明

| 类型 | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | feat(member): 添加会员购卡流程 |
| `fix` | Bug修复 | fix(payment): 修复支付回调失败问题 |
| `docs` | 文档更新 | docs: 更新API文档 |
| `style` | 代码格式 | style: 格式化代码 |
| `refactor` | 重构 | refactor(auth): 重构认证模块 |
| `test` | 测试相关 | test(store): 添加门店单元测试 |
| `chore` | 构建/工具 | chore: 更新依赖版本 |

### 模块名称

- `member` - 会员模块
- `store` - 门店模块
- `coach` - 教练模块
- `course` - 课程模块
- `payment` - 支付模块
- `order` - 订单模块
- `equipment` - 设备模块
- `manager` - 管理后台
- `auth` - 认证授权
- `common` - 公共模块
- `gateway` - 网关

---

## 禁止事项

1. **禁止直接push到master或develop分支**
2. **禁止使用 `git push --force`** (可以用 `--force-with-lease`)
3. **禁止在功能分支上开发多个不相关的功能**
4. **禁止提交敏感信息** (密钥、证书等)
5. **禁止提交IDE配置文件** (.idea目录已在.gitignore中)

---

## 常见问题

### Q: rebase时遇到冲突怎么办？

```bash
# 1. 查看冲突文件
git status

# 2. 手动解决冲突，编辑文件

# 3. 标记冲突已解决
git add <冲突文件>

# 4. 继续rebase
git rebase --continue

# 如果想放弃rebase
git rebase --abort
```

### Q: 提交了敏感信息怎么办？

```bash
# 如果还没push，修改最后一次提交
git reset HEAD~1
# 删除敏感文件后重新提交

# 如果已经push，联系管理员处理
```

### Q: 想撤销某次提交怎么办？

```bash
# 创建一个新的提交来撤销
git revert <commit-hash>
```

---

## Gitee 仓库设置建议

### 分支保护规则

对 `master` 和 `develop` 分支设置:
- [x] 启用分支保护
- [x] 禁止强制推送
- [x] 要求 Pull Request 审查
- [x] 要求通过状态检查才能合并

### 合并设置

- [x] 默认使用 Squash Merge
- [x] 合并后自动删除源分支

---

## 实际操作案例

### 案例：合并 feature/manager 分支到 develop

以下是一个完整的管理员合并操作记录，展示如何处理一个不太规范的功能分支。

#### 1. 检查分支状态

```bash
# 更新远程分支信息
git fetch origin

# 查看该分支独有的提交
git log origin/develop..origin/feature/manager --oneline
# 输出:
# 90c9e56 Merge remote-tracking branch 'refs/remotes/upstream/master' into feature/manager
# 7d69bf9 测试修复manager接口

# 查看改动的文件
git diff origin/develop..origin/feature/manager --stat
# 发现: 删除了5个Controller，修改了OrderMapper相关文件
```

**发现问题**: 分支有 merge 提交，说明队员没有使用 rebase，需要用方式三处理。

#### 2. 确保 develop 是最新的

```bash
git checkout develop
git pull origin develop
```

#### 3. 拉取功能分支到本地

```bash
# 使用临时分支名，避免污染本地分支
git fetch origin feature/manager:feature/manager-local
```

#### 4. Rebase 到最新 develop

```bash
git checkout feature/manager-local

# 如果有未提交的更改，先 stash
git stash

# 执行 rebase
git rebase develop
# 输出: Successfully rebased and updated refs/heads/feature/manager-local
```

#### 5. Squash Merge 到 develop

```bash
git checkout develop
git merge --squash feature/manager-local

# 检查暂存的更改
git status
# 发现有不需要的 .idea 文件被添加

# 排除不需要的文件
git restore --staged .idea/dataSources.xml .idea/sqldialects.xml
```

#### 6. 提交合并

```bash
git commit -m "refactor(manager): 重构订单模块，移除冗余的分析控制器

- 删除 MemberAnalysisController 和 MemberAnalysisTimeFilterController
- 删除 OrderController, ProfitSharingController, ProfitSharingTimeFilterController
- 重构 OrderMapper 和 OrderService 接口
- 添加 test_order.sql 测试脚本"
```

#### 7. 推送并清理分支

```bash
# 推送到远程
git push origin develop

# 删除本地临时分支
git branch -D feature/manager-local

# 删除远程功能分支
git push origin --delete feature/manager

# 恢复之前 stash 的更改（如果有）
git stash pop
```

#### 8. 合并结果

| 项目 | 结果 |
|------|------|
| 合并方式 | Squash Merge (方式三) |
| 删除文件 | 5 个 Controller |
| 修改文件 | 4 个 (OrderMapper 相关) |
| 新增文件 | test_order.sql |
| 提交信息 | `refactor(manager): 重构订单模块，移除冗余的分析控制器` |
| 分支清理 | feature/manager 已删除 |

#### 关键点总结

1. **检查分支状态** - 发现有 merge 提交，决定使用方式三
2. **使用临时分支** - `feature/xxx-local` 避免污染本地
3. **Rebase 后再合并** - 保持历史线性
4. **排除不需要的文件** - 合并时发现有 .idea 文件，手动排除
5. **规范的提交信息** - 使用 `refactor(manager):` 格式
6. **清理分支** - 合并后删除本地和远程分支
