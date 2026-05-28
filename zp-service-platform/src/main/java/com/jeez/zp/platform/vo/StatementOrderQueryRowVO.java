package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class StatementOrderQueryRowVO {

    private String orderId;
    private String customerInfo;
    private String productType;
    private String productName;
    private String bookingTime;
    private String channelName;
    private Long payableAmountCent;
    private Long paidAmountCent;
    private Long discountAmountCent;
    private Long refundAmountCent;
    private Long paymentFeeCent;
    private Long platformServiceFeeCent;
    private Long distributorCommissionCent;
    private String paymentWayName;
    private Long settlementAmountCent;
}
