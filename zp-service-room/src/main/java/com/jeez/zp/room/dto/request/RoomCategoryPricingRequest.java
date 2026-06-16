package com.jeez.zp.room.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryPricingRequest {

    private String campId;
    private List<String> roomCategoryIds;
    private List<String> channelIds;
    private List<String> poiIds;
    private String date;
    private String startDate;
    private Integer days;
    private Integer pageNum;
    private Integer pageSize;
    private Integer discountType;
}
