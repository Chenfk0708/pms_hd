package com.jeez.zp.order.vo;

import lombok.Data;

@Data
public class ChannelCallbackOrderOperationResponseVO {

    private String accountId;
    private String outOrderNo;
    private String orderId;
    private String status;
    private String message;

    public static ChannelCallbackOrderOperationResponseVO of(
            String accountId,
            String outOrderNo,
            String orderId,
            String status,
            String message
    ) {
        ChannelCallbackOrderOperationResponseVO response = new ChannelCallbackOrderOperationResponseVO();
        response.setAccountId(accountId);
        response.setOutOrderNo(outOrderNo);
        response.setOrderId(orderId);
        response.setStatus(status);
        response.setMessage(message);
        return response;
    }
}
