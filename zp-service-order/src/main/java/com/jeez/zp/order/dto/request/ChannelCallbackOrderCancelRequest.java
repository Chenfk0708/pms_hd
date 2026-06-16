package com.jeez.zp.order.dto.request;

import lombok.Data;

@Data
public class ChannelCallbackOrderCancelRequest {

    private String accountId;
    private String outOrderNo;
    private String orderId;
    private String campId;
    private String reason;
    private Object rawPayload;
}
