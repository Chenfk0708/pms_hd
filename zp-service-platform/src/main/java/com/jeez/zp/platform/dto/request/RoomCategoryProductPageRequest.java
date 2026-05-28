package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class RoomCategoryProductPageRequest {

    private String campId;
    private String keyword;
    private String roomCategoryId;
    private String channelId;
    private Integer pageNum;
    private Integer pageSize;
}
