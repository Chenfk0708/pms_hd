package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class CleanLogFilterOptionsVO {

    private List<CleanLogOptionVO> stores;
    private List<CleanLogRoomOptionVO> rooms;
    private List<CleanLogOptionVO> operators;
}
