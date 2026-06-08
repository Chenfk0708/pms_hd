package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class DistributionOrderSummaryVO {
    private Double paidAmount;
    private Double serviceFee;
    private Double settlementAmount;
    private Double settledAmount;
    private Double invoicePrice;
    private Double commission;
    private Double incomePrice;
    private Double settledPrice;
}
