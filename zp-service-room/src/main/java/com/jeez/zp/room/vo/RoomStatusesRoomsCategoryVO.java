package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomStatusesRoomsCategoryVO {

    private String campId;
    private String storeId;
    private String storeName;
    private String roomCategoryId;
    private String roomCategoryName;
    private List<RoomStatusesRoomsRoomVO> rooms;
}
