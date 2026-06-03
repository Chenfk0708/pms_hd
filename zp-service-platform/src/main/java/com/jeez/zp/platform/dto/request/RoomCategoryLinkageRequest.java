package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryLinkageRequest {

    private String campId;
    private String roomCategoryId;
    private List<String> linkedRoomCategoryIds;
}
