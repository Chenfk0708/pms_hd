package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_member")
public class PmsMember {

    @TableId("member_id")
    private Long memberId;
    private Long campId;
    private Long roleId;
    private Long userId;
    private String name;
    private String mobile;
    private String email;
    private String wecomUserId;
    private Integer status;
    private LocalDateTime lastLoginAt;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer isDeleted;
    private Integer versionNo;
}
