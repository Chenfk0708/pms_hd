package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_poi")
public class PmsPoi {

    @TableId("poi_id")
    private Long poiId;
    private Long campId;
    private String poiName;
    private String poiType;
    private Integer isAvailability;
    private Integer sortNo;
    private String address;
    private String contactNumber;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer isDeleted;
    private Integer versionNo;
}
