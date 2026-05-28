package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryPricingRequest {

    private String campId;
    private List<String> roomCategoryIds;
    private List<String> channelIds;
    private Integer discountType;
}
