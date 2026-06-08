package com.jeez.zp.finance.vo;

import lombok.Data;

import java.util.List;

@Data
public class AccountBookPaymentWayExtraInfoVO {
    private List<PaymentWayAmountVO> income;
    private List<PaymentWayAmountVO> expend;
    private AccountBookPaymentWayTotalInfoVO totalInfo;
}
