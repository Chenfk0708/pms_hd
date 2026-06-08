package com.jeez.zp.order.vo;

import lombok.Data;

@Data
public class OrderChangeRoomOptionVO {

    private String roomId;
    private String roomName;
    private String roomCategoryId;
    private String roomCategoryName;
    private String poiId;
    private String poiName;
}
