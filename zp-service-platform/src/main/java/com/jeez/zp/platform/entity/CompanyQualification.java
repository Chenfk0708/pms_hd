package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("company_qualification")
public class CompanyQualification {

    @TableId("qualification_id")
    private Long qualificationId;
    private Long campId;
    private String documentType;
    private String documentNumber;
    private String legalPersonName;
    private String legalPersonIdNumber;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
