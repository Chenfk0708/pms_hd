package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("payment_way")
public class PaymentWay {

    @TableId("payment_way_id")
    private Long paymentWayId;
    private Long campId;
    private String paymentWayName;
    private String paymentWayCode;
    private String wayType;
    private Integer sortNo;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
