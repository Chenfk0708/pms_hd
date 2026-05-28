package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class StatementOrderRowVO {

    private String orderId;
    private String customerInfo;
    private String productType;
    private String productName;
    private String bookingTime;
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
