package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomTypeFloorResponseVO {

    private List<RoomTypeFloorVO> list;
    private List<RoomTypeFloorVO> rows;
    private List<RoomTypeOptionVO> roomTypeOptions;
}
