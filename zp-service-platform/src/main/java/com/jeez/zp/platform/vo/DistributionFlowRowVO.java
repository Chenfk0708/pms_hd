package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class DistributionFlowRowVO {

    private String orderNo;
    private String orderId;
    private String customerName;
    private String customerPhone;
    private String customerInfo;
    private String roomCategoryName;
    private String bookedTime;
    private String bookedTimeStr;
    private Double invoicePrice;
    private Double commission;
    private Double incomePrice;
    private Double settledPrice;
    private Double paidAmount;
    private Double serviceFee;
    private Double settlementAmount;
    private Double settledAmount;
    private String settlementStatus;
    private String settledState;
    private String orderFilter;
}
