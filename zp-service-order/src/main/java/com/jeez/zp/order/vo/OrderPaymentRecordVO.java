package com.jeez.zp.order.vo;

import lombok.Data;

@Data
public class OrderPaymentRecordVO {

    private String paymentRecordId;
    private String paymentTypeId;
    private String paymentWayId;
    private String paymentWayName;
    private Integer isIncome;
    private Long amount;
    private Long debtAmount;
    private String paymentNo;
    private String paymentTime;
    private String operatorName;
    private String remark;
}
