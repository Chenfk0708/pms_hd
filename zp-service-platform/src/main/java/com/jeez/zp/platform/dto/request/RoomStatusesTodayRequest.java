package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoomStatusesTodayRequest {

    private String campId;
    private List<String> channelIds;
    private List<String> roomCategoryGroupIds;
    private List<String> roomCategoryIds;
    private List<String> poiIds;
    private Object cleanStatus;
    private Object date;
    private Object queryCode;

    private String storeId;
    private String keyword;
    private String viewMode;
    private List<String> statusFilters;
    private String channel;
    private String roomType;
    private String tag;
}
