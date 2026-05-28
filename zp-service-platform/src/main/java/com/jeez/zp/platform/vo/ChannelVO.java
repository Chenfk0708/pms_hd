package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class ChannelVO {

    private Long channelId;
    private String channelName;
    private Long accountId;
    private String accountName;
    private Long poiId;
    private String syncStatus;
    private String status;
}
