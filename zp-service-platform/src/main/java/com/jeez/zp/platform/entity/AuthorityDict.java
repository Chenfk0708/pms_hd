package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("authority_dict")
public class AuthorityDict {

    @TableId("authority_id")
    private Long authorityId;
    private String authorityName;
    private String authorityCode;
    private String authorityType;
    private String moduleName;
    private String remark;
    private Integer seqNo;
    private Integer status;
    private LocalDateTime createdAt;
}
