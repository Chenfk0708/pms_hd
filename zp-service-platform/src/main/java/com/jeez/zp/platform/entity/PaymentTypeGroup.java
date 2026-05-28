package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("payment_type_group")
public class PaymentTypeGroup {

    @TableId("payment_type_group_id")
    private Long paymentTypeGroupId;
    private Long campId;
    private Integer groupType;
    private String groupName;
    private Integer bizType;
    private Integer isIncome;
    private Integer sortNo;
    private Integer status;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
