package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class ForwardRoomStatusRowVO {

    private String roomCategoryId;
    private String roomCategoryName;
    private Integer availabilityCount;
    private List<ForwardRoomStatusDayVO> forwardRoomStatusList;
}
