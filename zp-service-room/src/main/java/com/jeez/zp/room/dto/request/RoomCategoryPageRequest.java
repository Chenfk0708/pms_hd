package com.jeez.zp.room.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryPageRequest {

    private String campId;
    private Integer pageSize;
    private Integer pageNum;
    private Integer current;
    private String roomCategoryName;
    private String keyword;
    private List<Long> cityIds;
    private String channelId;
    private String poiId;
    private String roomCategoryGroupId;
}
