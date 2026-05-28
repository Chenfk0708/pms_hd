package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_camp")
public class PmsCamp {

    @TableId("camp_id")
    private Long campId;
    private String name;
    private Integer type;
    private String networkNum;
    private String provinceId;
    private String provinceName;
    private String cityId;
    private String cityName;
    private String countyId;
    private String countyName;
    private String address;
    private String contactNumber;
    private String logoMediaId;
    private String logoMediaUrl;
    private String consoleLogoMediaId;
    private String consoleLogoMediaUrl;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer isDeleted;
    private Integer versionNo;
}
