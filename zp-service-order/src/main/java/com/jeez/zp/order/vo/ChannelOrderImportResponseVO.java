package com.jeez.zp.order.vo;

import lombok.Data;

@Data
public class ChannelOrderImportResponseVO {

    private String orderId;
    private String outOrderNo;
    private String accountId;
    private String channelName;
    private String status;
    private Boolean created;
    private String message;
}
