package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_role")
public class PmsRole {

    @TableId("role_id")
    private Long roleId;
    private Long campId;
    private String roleName;
    private String roleCode;
    private Integer isSystem;
    private Integer status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer isDeleted;
    private Integer versionNo;
}
