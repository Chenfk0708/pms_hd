package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class RoomPageItemVO {

    private String roomId;
    private String id;
    private String roomName;
    private String name;
    private String campId;
    private String poiId;
    private String storeId;
    private String storeName;
    private String roomCategoryId;
    private String roomCategoryName;
    private String floorId;
    private String floorName;
    private Integer isDirty;
    private Integer isCanBooking;
    private String lockStatus;
    private String saleType;
    private String cleanStatus;
    private Integer seq;
}
