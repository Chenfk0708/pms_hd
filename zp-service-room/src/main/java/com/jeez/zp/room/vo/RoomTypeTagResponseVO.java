package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomTypeTagResponseVO {

    private List<RoomTypeTagVO> list;
    private List<RoomTypeTagVO> rows;
    private List<RoomTypeOptionVO> roomTypeOptions;
}
