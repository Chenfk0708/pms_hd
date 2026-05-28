package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class DistributionFlowQueryRowVO {

    private String orderNo;
    private String customerName;
    private String customerPhone;
    private String roomCategoryName;
    private String bookedTime;
    private Long invoicePriceCent;
    private Long commissionCent;
    private Long incomePriceCent;
    private Long settledPriceCent;
    private String settledState;
    private Boolean breakTemp;
}
