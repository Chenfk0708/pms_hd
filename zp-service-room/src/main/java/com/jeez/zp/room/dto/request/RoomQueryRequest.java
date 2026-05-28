package com.jeez.zp.room.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoomQueryRequest {

    private String campId;
    private List<String> roomCategoryIds;
    private Integer saleType;
}
