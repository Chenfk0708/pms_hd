package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class DistributionOrderSummaryVO {

    private Double invoicePrice;
    private Double commission;
    private Double incomePrice;
    private Double settledPrice;
}
