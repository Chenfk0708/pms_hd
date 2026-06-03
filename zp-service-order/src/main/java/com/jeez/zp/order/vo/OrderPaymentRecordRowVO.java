package com.jeez.zp.order.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderPaymentRecordRowVO {

    private String paymentRecordId;
    private String paymentTypeId;
    private String paymentWayId;
    private String paymentWayName;
    private Integer isIncome;
    private Long amount;
    private Long debtAmount;
    private String paymentNo;
    private LocalDateTime paymentTime;
    private String operatorName;
    private String remark;
}
