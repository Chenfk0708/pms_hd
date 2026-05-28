package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("payment_type")
public class PaymentType {

    @TableId("payment_type_id")
    private Long paymentTypeId;
    private Long campId;
    private Long paymentTypeGroupId;
    private String paymentTypeName;
    private Integer groupType;
    private String groupName;
    private Integer bizType;
    private Integer isIncome;
    private Integer isCustom;
    private Integer ignoreOrderGetItem;
    private Integer status;
    private Integer sortNo;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
