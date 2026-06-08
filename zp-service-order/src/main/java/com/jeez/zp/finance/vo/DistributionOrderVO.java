package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class DistributionOrderVO {
    private String distributionOrderId;
    private String sourceOrderId;
    private String channelId;
    private String orderId;
    private String customerName;
    private String customerPhone;
    private String customerInfo;
    private String roomCategoryName;
    private String bookedTime;
    private Double paidAmount;
    private Double serviceFee;
    private Double settlementAmount;
    private Double settledAmount;
    private String settledState;
    private String settlementStatus;
    private String orderFilter;
    private Double invoicePrice;
    private Double commission;
    private Double incomePrice;
    private Double settledPrice;
    private Double commissionPrice;
    private String createdAt;
}
