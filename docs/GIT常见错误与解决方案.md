# Git 常见错误与解决方案

本文档记录了项目开发过程中遇到的 Git 问题，分析原因并提供解决方案。

---

## 问题一：合并代码导致回滚

### 现象

合并队员的功能分支后，发现：
- 之前删除的文件又回来了
- 之前添加的文件被删除了
- 代码回退到了旧版本

### 实际案例

```
合并 feature/equipment-fitness 分支时发现：
- 会删除所有 runConfigurations（10个运行配置）
- 会删除 .gitignore 的大部分规则
- 会删除 Dockerfile
- 会添加回我们已删除的 .idea/compiler.xml
```

### 原因分析

**根本原因：功能分支基于老代码，长期未同步主分支**

```
时间线：
Day 1: develop 在 commit A
Day 1: 队员从 A 创建 feature/xxx 分支
Day 2-10: develop 继续开发，到达 commit M
Day 10: 队员在 feature/xxx 提交，但基于 commit A

结果：feature/xxx 相对于 develop 的"差异"实际上会撤销 A→M 的所有改动
```

图示：
```
develop:  A → B → C → D → E → F → G → H → I → J → K → L → M (最新)
                ↘
feature/xxx:     A → X → Y (队员的提交)

合并时 Git 认为：feature/xxx 相对于 A 的改动是 X 和 Y
但实际上 X 和 Y 可能会覆盖 B→M 的改动！
```

### 解决方案

#### 预防措施（开发者）

```bash
# 每天开始工作前，同步最新代码
git fetch origin
git rebase origin/develop

# 提交 PR 前，必须先 rebase
git fetch origin
git rebase origin/develop
git push --force-with-lease
```

#### 修复措施（管理员）

```bash
# 方法1：Cherry-pick 只提取有效提交
git cherry-pick <commit-hash> --no-commit
# 排除不需要的文件
git restore --staged <不需要的文件>
git commit -m "feat: 描述"

# 方法2：帮队员 rebase
git fetch origin feature/xxx:feature/xxx-local
git checkout feature/xxx-local
git rebase develop
# 解决冲突...
git checkout develop
git merge --squash feature/xxx-local
```

---

## 问题二：.idea 等配置文件反复出现

### 现象

- `.idea/compiler.xml` 被反复添加和删除
- `.idea/dataSources.xml` 不断出现在提交中
- `dump.rdb` (Redis数据文件) 被提交

### 实际案例

```
git log 显示：
commit A: 删除 .idea/compiler.xml
commit B: 添加 .idea/compiler.xml  ← 又回来了
commit C: 删除 .idea/compiler.xml
commit D: 添加 .idea/compiler.xml  ← 又回来了
```

### 原因分析

1. **队员本地有这些文件**，提交时没有检查
2. **旧分支包含这些文件**，合并时带入
3. **.gitignore 配置不完整**，新生成的文件没有被忽略
4. **已跟踪的文件不受 .gitignore 影响**

### 解决方案

#### 1. 完善 .gitignore

```gitignore
# 直接忽略整个 .idea 目录
.idea/

# 忽略 Redis 数据文件
dump.rdb

# 忽略敏感配置
*.pem
*.key
.env
```

#### 2. 从 Git 中移除已跟踪的文件

```bash
# 从 Git 移除，但保留本地文件
git rm --cached .idea/compiler.xml
git rm --cached dump.rdb

# 提交
git commit -m "chore: 移除不应跟踪的文件"
```

#### 3. 锁定文件不再检测变更

```bash
# 对于需要保留在仓库但不想检测变更的文件
git update-index --skip-worktree .idea/misc.xml

# 查看哪些文件被锁定
git ls-files -v | grep "^S"

# 解除锁定
git update-index --no-skip-worktree .idea/misc.xml
```

---

## 问题三：Merge 提交导致历史混乱

### 现象

```
git log --graph 显示复杂的分支图：
*   Merge branch 'xxx'
|\
| * commit
* | commit
|\ \
| * | commit
* | | commit
| |/
|/|
```

### 原因分析

队员使用 `git pull` 或 `git merge` 同步代码，而不是 `git rebase`

```bash
# 错误做法（产生 merge 提交）
git pull origin develop

# 正确做法（保持历史线性）
git fetch origin
git rebase origin/develop
```

### 解决方案

#### 开发者配置

```bash
# 设置 pull 默认使用 rebase
git config --global pull.rebase true

# 以后 git pull 就等于 git pull --rebase
```

#### 管理员合并时

```bash
# 使用 squash merge，把所有提交压缩成一个
git merge --squash feature/xxx

# 这样无论队员的分支多乱，合并后都是一个干净的提交
```

---

## 问题四：重复提交信息

### 现象

```
git log 显示：
abc1234 修复门店公告管理接口
def5678 修复门店公告管理接口  ← 重复
ghi9012 修复门店公告管理接口  ← 重复
```

### 原因分析

1. **多人在同一分支开发**，各自提交相同内容
2. **merge 后又 merge**，产生重复
3. **reset 后重新提交**，但旧提交还在

### 解决方案

#### 使用 Squash Merge

```bash
# 合并时压缩所有提交为一个
git merge --squash feature/xxx
git commit -m "feat(store): 添加门店公告管理功能"
```

#### 清理本地分支历史

```bash
# 交互式 rebase，合并多个提交
git rebase -i HEAD~5

# 在编辑器中将重复的 pick 改为 squash
pick abc1234 第一个提交
squash def5678 合并到上一个
squash ghi9012 合并到上一个
```

---

## 问题五：敏感信息被提交

### 现象

- 数据库密码出现在 `application.yml`
- 证书文件 `*.pem` 被提交
- `.env` 文件被提交

### 解决方案

#### 预防

```gitignore
# .gitignore 中添加
*.pem
*.key
*.p12
.env
**/application-local.yml
**/application-prod.yml
```

#### 已提交后处理

```bash
# 如果还没 push
git reset HEAD~1
# 删除敏感文件后重新提交

# 如果已经 push（需要通知所有人）
# 1. 使用 BFG 清理历史
bfg --delete-files *.pem
git push --force

# 2. 立即更换泄露的密钥/密码！
```

---

## 问题六：冲突太多无法合并

### 现象

```
git merge 或 git rebase 时：
CONFLICT (content): Merge conflict in file1.java
CONFLICT (content): Merge conflict in file2.java
CONFLICT (content): Merge conflict in file3.java
... 几十个冲突
```

### 原因分析

功能分支和主分支差异太大，通常是：
1. 分支创建时间太久
2. 长期未同步主分支
3. 主分支有大规模重构

### 解决方案

#### 方法1：逐个 Cherry-pick 有效提交

```bash
# 找出功能分支独有的提交
git log develop..feature/xxx --oneline

# 只 cherry-pick 需要的提交
git cherry-pick <commit1>
git cherry-pick <commit2>
```

#### 方法2：让开发者重新开发

```bash
# 从最新 develop 创建新分支
git checkout develop
git checkout -b feature/xxx-v2

# 手动复制需要的代码改动
# 重新提交
```

#### 方法3：分批解决冲突

```bash
git rebase develop

# 解决第一个冲突
git add <file>
git rebase --continue

# 解决第二个冲突...
# 重复直到完成
```

---

## 最佳实践总结

### 开发者每日必做

```bash
# 1. 开始工作前同步
git fetch origin
git rebase origin/develop

# 2. 提交前检查
git status  # 确认没有奇怪的文件
git diff --cached  # 确认改动内容

# 3. 使用规范的提交信息
git commit -m "feat(module): 描述"
```

### 管理员合并检查清单

```bash
# 1. 查看分支有哪些提交
git log origin/develop..origin/feature/xxx --oneline

# 2. 查看改动了哪些文件
git diff origin/develop..origin/feature/xxx --stat

# 3. 检查敏感文件
git diff origin/develop..origin/feature/xxx --name-only | grep -E '\.(env|pem|key)'

# 4. 检查是否会回滚代码
git diff origin/develop..origin/feature/xxx --stat | grep -E "^\s*D"
```

### 项目配置建议

```bash
# 团队成员都执行
git config --global pull.rebase true
git config --global rebase.autoStash true
```

---

## 快速参考

| 问题 | 快速解决 |
|------|----------|
| 合并导致回滚 | `git cherry-pick <commit> --no-commit` |
| .idea 文件反复出现 | `git rm --cached .idea/*` |
| 历史太乱 | `git merge --squash feature/xxx` |
| 冲突太多 | 逐个 cherry-pick 或重新开发 |
| 敏感信息泄露 | 立即更换密钥 + BFG 清理历史 |
| 想撤销合并 | `git revert -m 1 <merge-commit>` |
