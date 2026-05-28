package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class SelectRoomCategoryPageRequest {

    private String campId;
    private Integer pageSize;
    private Integer pageNum;
    private Integer current;
    private String poiId;
    private String channelId;
    private String filterSyncChannelId;
    private String isAvailability;
    private Integer isFilterAlreadyFlow;
}
