package com.jeez.zp.order.vo;

import lombok.Data;

@Data
public class ChannelOrderMappingVO {

    private Long accountId;
    private Long campId;
    private Long channelId;
    private String channelName;
    private Long poiId;
    private String poiName;
    private Long roomCategoryId;
    private String roomCategoryName;
}
