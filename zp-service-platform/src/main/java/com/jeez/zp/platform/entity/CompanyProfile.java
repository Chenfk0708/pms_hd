package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("company_profile")
public class CompanyProfile {

    @TableId("company_profile_id")
    private Long companyProfileId;
    private Long campId;
    private String companyName;
    private String companyType;
    private String phone;
    private String cityText;
    private String address;
    private Long coverMediaId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
