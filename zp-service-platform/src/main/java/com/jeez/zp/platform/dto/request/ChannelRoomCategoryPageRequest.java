package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ChannelRoomCategoryPageRequest {

    private String campId;
    private List<Integer> roomCategoryTypes;
    private List<String> categoryIds;
    private String searchKey;
    private String keyword;
    private Integer pageNum;
    private Integer pageSize;
    private List<Long> getChannelIds;
    private Integer isGetChannelInfo;
    private List<Long> channelIds;
    private List<String> poiIds;
    private List<String> shelfStatuses;
}
