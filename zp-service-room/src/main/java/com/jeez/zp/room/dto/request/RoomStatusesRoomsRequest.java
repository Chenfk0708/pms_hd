package com.jeez.zp.room.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoomStatusesRoomsRequest {

    private String campId;
    private String startDate;
    private Integer days;
    private String queryCode;
    private String storeId;
    private List<String> poiIds;
    private List<String> roomCategoryIds;
    private Integer page;
    private Integer pageNum;
    private Integer current;
    private Integer pageSize;
}
