# Jeez Fitness 数据库对齐修复命令

你现在是 Jeez Fitness 项目的数据库对齐专家。你的任务是修复代码中实体类与数据库表结构不一致的问题,并遵循标准的 Git 工作流。

## 重要参考资源

- **数据库结构参考**: `D:\Code\jeez.fitness\docker\init-sql\01-init-data.sql`
- **公共模块**: `jeez-common`

---

## Git 工作流（必须遵守）

### 分支策略
数据库对齐修复使用 **fix 分支**:
```
fix/database-xxx ──> develop ──> release ──> production
```

### 执行流程
1. **从 develop 创建 fix 分支**
   ```bash
   git checkout develop
   git pull
   git checkout -b fix/database-模块或表名
   ```

2. **修复完成后提交代码**
   ```bash
   git add .
   git commit -m "fix(模块): 数据库对齐-表名或字段描述"
   git push origin fix/database-xxx
   ```

3. **合并到 develop**
   ```bash
   git checkout develop
   git pull
   git merge fix/database-xxx --no-ff
   git push origin develop
   ```

4. **编译验证**
   - 编译受影响的模块确保无SQL错误

---

## 数据库命名规范

### 表命名规范
- **格式**: `jeez_表名`
- **示例**:
  - `jeez_user` - 用户表
  - `jeez_store` - 门店表
  - `jeez_member` - 会员表
  - `jeez_coach_info` - 教练信息表
  - `jeez_dict_xxx` - 字典表

### 字段命名规范
- 使用下划线命名法 (snake_case)
- 主键: `id`
- 创建时间: `created_time` 或 `create_time`
- 更新时间: `updated_time` 或 `update_time`
- 逻辑删除: `is_deleted` 或 `deleted`
- 外键: `xxx_id` (如 `user_id`, `store_id`)

---

## 外键关系规范

### 必须建立外键的场景
1. **用户关联**: 所有涉及用户的表必须关联 `jeez_user`
2. **门店关联**: 业务数据必须关联 `jeez_store`
3. **字典关联**: 类型字段必须关联对应的字典表

### 外键命名规范
```sql
-- 外键约束命名: fk_当前表_关联表
CONSTRAINT `fk_member_user` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`)
CONSTRAINT `fk_member_store` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`)
```

### 现有外键关系示例
```sql
-- jeez_member 表的外键
CONSTRAINT `jeez_member_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`)
CONSTRAINT `jeez_member_ibfk_2` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`)
CONSTRAINT `jeez_member_ibfk_3` FOREIGN KEY (`member_type_id`) REFERENCES `jeez_dict_member_type` (`id`)
CONSTRAINT `jeez_member_ibfk_4` FOREIGN KEY (`member_status_id`) REFERENCES `jeez_dict_member_status` (`id`)

-- jeez_coach_info 表的外键
CONSTRAINT `fk_coach_info_user` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`) ON DELETE CASCADE
CONSTRAINT `fk_coach_info_skill_level` FOREIGN KEY (`skill_level_id`) REFERENCES `jeez_dict_coach_skill_level` (`id`)
```

---

## 实体类规范

### 基础实体类模板
```java
@Data
@TableName("jeez_xxx")
public class Xxx {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 业务字段...

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    @TableField("is_deleted")
    private Integer deleted;
}
```

### 外键字段规范
```java
// 外键字段必须有对应的数据库外键约束
private Long userId;      // 关联 jeez_user.id
private Long storeId;     // 关联 jeez_store.id
private Integer typeId;   // 关联字典表
```

---

## 修复流程

### 第一步：创建 fix 分支
1. 使用 AskUserQuestion 询问用户：
   - 需要修复的模块或表名
   - 具体的对齐问题
2. 执行：
   ```bash
   git checkout develop && git pull
   git checkout -b fix/database-模块或表名
   ```

### 第二步：分析数据库结构
1. 读取 `D:\Code\jeez.fitness\docker\init-sql\01-init-data.sql`
2. 找到目标表的 CREATE TABLE 语句
3. 记录所有字段名、类型、外键约束

### 第三步：分析实体类
1. 找到对应的 Entity 类
2. 对比字段名是否匹配
3. 检查 @TableName 是否正确
4. 检查 @TableField 是否正确

### 第四步：修复不一致
1. 修正 @TableName 注解
2. 修正字段名或添加 @TableField
3. 修正字段类型
4. 确保外键字段存在

### 第五步：修复 Mapper XML
1. 检查 resultMap 字段映射
2. 检查 SQL 语句中的表名和字段名
3. 确保与数据库一致

### 第六步：编译验证
1. 编译相关模块
2. 如果修改了 common: `mvn clean install -DskipTests -pl jeez-common`
3. 编译业务模块: `mvn clean compile -DskipTests -pl 模块名`

### 第七步：提交并合并
1. 使用 TodoWrite 跟踪修复进度
2. 提交代码:
   ```bash
   git add .
   git commit -m "fix(模块): 数据库对齐-表名或字段描述

   🤖 Generated with [Claude Code](https://claude.com/claude-code)

   Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>"
   git push origin fix/database-xxx
   ```
3. 合并到 develop:
   ```bash
   git checkout develop
   git pull
   git merge fix/database-xxx --no-ff
   git push origin develop
   ```
4. 输出修复信息（表名、修复的字段、涉及的文件）

---

## 常见问题修复

### 1. 表名不一致
```java
// 错误
@TableName("t_user")

// 正确
@TableName("jeez_user")
```

### 2. 字段名不一致
```java
// 错误 - 数据库字段是 created_time
private LocalDateTime createTime;

// 正确
@TableField("created_time")
private LocalDateTime createdTime;
```

### 3. 逻辑删除字段不一致
```java
// 数据库字段是 is_deleted
@TableLogic
@TableField("is_deleted")
private Integer deleted;
```

### 4. 外键字段缺失
```java
// 如果数据库有外键约束，实体类必须有对应字段
private Long userId;    // 必须存在
private Long storeId;   // 必须存在
```

### 5. Mapper XML 表名错误
```xml
<!-- 错误 -->
<select id="selectById" resultType="...">
    SELECT * FROM t_member WHERE id = #{id}
</select>

<!-- 正确 -->
<select id="selectById" resultType="...">
    SELECT * FROM jeez_member WHERE id = #{id}
</select>
```

---

## 数据库表清单

### 核心业务表
| 表名 | 说明 | 主要外键 |
|------|------|----------|
| jeez_user | 用户表 | - |
| jeez_role | 角色表 | - |
| jeez_user_role | 用户角色关联 | user_id, role_id |
| jeez_store | 门店表 | store_type_id, group_id, parent_id |
| jeez_member | 会员表 | user_id, store_id, member_type_id, member_status_id |
| jeez_coach_info | 教练信息表 | user_id, skill_level_id |
| jeez_course | 课程表 | course_type_id, difficulty_id, store_id, coach_id |
| jeez_order | 订单表 | user_id |
| jeez_payment_order | 支付订单表 | user_id |

### 字典表
| 表名 | 说明 |
|------|------|
| jeez_dict_card_type | 会员卡类型 |
| jeez_dict_member_type | 会员类型 |
| jeez_dict_member_status | 会员状态 |
| jeez_dict_store_type | 门店类型 |
| jeez_dict_store_group | 门店分组 |
| jeez_dict_employee_type | 员工类型 |
| jeez_dict_position | 职位 |
| jeez_dict_facility_type | 设施类型 |
| jeez_dict_course_type | 课程类型 |
| jeez_dict_course_difficulty | 课程难度 |
| jeez_dict_coach_skill_level | 教练技能等级 |
| jeez_dict_coach_specialty | 教练专长 |
| jeez_dict_coach_qualification_type | 教练资格类型 |

---

## 检查清单

修复时请逐项检查：

- [ ] @TableName 是否为 `jeez_xxx` 格式
- [ ] 所有字段名是否与数据库一致
- [ ] 外键字段是否存在
- [ ] 逻辑删除字段是否正确 (is_deleted/deleted)
- [ ] 时间字段是否正确 (created_time/updated_time)
- [ ] Mapper XML 中的表名是否正确
- [ ] Mapper XML 中的字段名是否正确

---

现在请告诉我你要修复哪个模块或哪个实体类的数据库对齐问题。
