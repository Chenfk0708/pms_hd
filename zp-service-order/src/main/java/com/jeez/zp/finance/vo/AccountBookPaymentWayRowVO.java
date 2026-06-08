package com.jeez.zp.finance.vo;

import lombok.Data;

import java.util.List;

@Data
public class AccountBookPaymentWayRowVO {
    private String date;
    private List<PaymentWayAmountVO> paymentWayPriceDetailViews;
}
