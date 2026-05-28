package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomStatusesTodayFloorVO {

    private String floorId;
    private String floorName;
    private List<RoomStatusesTodayRoomVO> rooms;
}
