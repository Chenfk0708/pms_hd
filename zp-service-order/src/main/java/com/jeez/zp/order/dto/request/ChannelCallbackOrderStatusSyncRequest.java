package com.jeez.zp.order.dto.request;

import lombok.Data;

@Data
public class ChannelCallbackOrderStatusSyncRequest {

    private String accountId;
    private String outOrderNo;
    private String orderId;
    private String campId;
    private String channelStatus;
    private String paymentStatus;
    private String reason;
    private Object rawPayload;
}
