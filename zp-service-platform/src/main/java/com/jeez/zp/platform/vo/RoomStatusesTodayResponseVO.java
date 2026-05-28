package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomStatusesTodayResponseVO {

    private RoomStatusesTodayBasicVO basic;
    private List<RoomStatusesTodayCategoryVO> roomCategories;
    private List<RoomStatusesTodayRoomVO> roomViews;
    private List<RoomStatusesTodayFloorVO> floorViews;
    private Integer isInitFloor;
}
