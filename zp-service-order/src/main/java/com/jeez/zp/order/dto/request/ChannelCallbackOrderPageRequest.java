package com.jeez.zp.order.dto.request;

import lombok.Data;

@Data
public class ChannelCallbackOrderPageRequest {

    private String accountId;
    private String campId;
    private Integer pageNum;
    private Integer current;
    private Integer pageSize;
    private String orderType;
    private String keyword;
    private String searchContent;
    private String channelStatus;
    private String orderStatus;
    private String outOrderNo;
}
