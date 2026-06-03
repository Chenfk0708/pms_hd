package com.jeez.zp.room.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoomPageRequest {

    private String campId;
    private String poiId;
    private String storeId;
    private List<String> roomCategoryIds;
    private Integer isAvailability;
    private Integer saleType;
    private String checkInDate;
    private String checkOutDate;
    private String keyword;
    private Integer pageNum;
    private Integer current;
    private Integer pageSize;
}
