package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class PaymentWayAmountVO {
    private String paymentWayId;
    private String paymentWayName;
    private Double price;
}
