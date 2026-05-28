# 构建并发布到测试环境

## 执行步骤

### 1. 检查状态并询问用户
```bash
git status --short && git branch --show-current
```

使用 AskUserQuestion 询问：
- 如何处理当前代码？（提交修改 / 暂存 / 放弃）
- 变更描述（简短说明）

### 2. 提交到 develop
```bash
# 如果有未提交代码
git add -A && git commit -m "描述" && git push origin develop
```

### 3. 编译所有模块
直接执行构建脚本（无需 TodoWrite）：
```bash
scripts\build-all.bat
```

**重要**: 脚本返回非零则停止，不推送 release！

### 4. 推送到 release（仅当脚本成功）
```bash
git checkout release && git pull origin release && git merge develop --no-ff -m "chore(release): [变更描述]" && git push origin release && git checkout develop
```

### 5. 输出结果
简洁报告：编译是否成功，release 是否已推送。
