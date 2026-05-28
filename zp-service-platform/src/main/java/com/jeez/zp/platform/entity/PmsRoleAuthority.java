package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_role_authority")
public class PmsRoleAuthority {

    @TableId("id")
    private Long id;
    private Long campId;
    private Long roleId;
    private Long authorityId;
    private String authorityType;
    private LocalDateTime createdAt;
    private Long createdBy;
}
