package com.jeez.zp.room.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryStatusRequest {

    private String campId;
    private List<String> channelIds;
    private List<String> roomCategoryIds;
    private List<String> poiIds;
    private List<String> roomCategoryGroupIds;
    private String date;
    private Integer days;
    private Integer pageNum;
    private Integer pageSize;
    private Integer isFinalChannelRp;
    private Integer isStores;
}
