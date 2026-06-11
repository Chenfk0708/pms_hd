package com.jeez.zp.order.vo;

import lombok.Data;

@Data
public class ChannelOrderRawVO {

    private Long rawId;
    private Long accountId;
    private String outOrderNo;
    private Long pmsOrderId;
    private String importStatus;
    private String rawPayload;
}
