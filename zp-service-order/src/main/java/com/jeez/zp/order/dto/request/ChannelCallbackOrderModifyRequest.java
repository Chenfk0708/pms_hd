package com.jeez.zp.order.dto.request;

import lombok.Data;

@Data
public class ChannelCallbackOrderModifyRequest {

    private String accountId;
    private String outOrderNo;
    private String orderId;
    private String campId;
    private String contactName;
    private String contactMobile;
    private String checkInDate;
    private String checkOutDate;
    private Long totalPrice;
    private Long totalPayPrice;
    private Long commissionPrice;
    private String paymentStatus;
    private String remark;
    private Object rawPayload;
}
