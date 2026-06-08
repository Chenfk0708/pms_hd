package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class StatementOrderRowVO {
    private String orderId;
    private String orderNo;
    private String customerInfo;
    private String customerName;
    private String mobile;
    private String productType;
    private String productTypeName;
    private String productName;
    private String bookingTime;
    private String bookingTimeStr;
    private String channelName;
    private Double payableAmount;
    private Double paidAmount;
    private Double discountAmount;
    private Double refundAmount;
    private Double paymentFee;
    private Double platformServiceFee;
    private Double distributorCommission;
    private String paymentWayName;
    private Double settlementAmount;
}
