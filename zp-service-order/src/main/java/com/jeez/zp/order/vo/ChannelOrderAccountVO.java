package com.jeez.zp.order.vo;

import lombok.Data;

@Data
public class ChannelOrderAccountVO {

    private Long accountId;
    private Long campId;
    private Long channelId;
    private String channelName;
    private String channelCode;
    private String status;
}
