package com.jeez.zp.order.dto.request;

import lombok.Data;

@Data
public class ChannelCallbackOrderDetailRequest {

    private String accountId;
    private String outOrderNo;
    private String orderId;
    private String campId;
}
