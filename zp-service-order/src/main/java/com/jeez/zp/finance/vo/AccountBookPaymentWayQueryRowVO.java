package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class AccountBookPaymentWayQueryRowVO {
    private String bizDate;
    private String paymentWayId;
    private String paymentWayName;
    private Integer sortNo;
    private Long incomeAmountCent;
    private Long expendAmountCent;
}
