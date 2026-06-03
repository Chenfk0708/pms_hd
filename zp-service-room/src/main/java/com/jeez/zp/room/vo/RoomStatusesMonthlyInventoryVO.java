package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class RoomStatusesMonthlyInventoryVO {

    private String roomCategoryId;
    private String date;
    private Integer inventory;
    private Integer inv;
    private Integer remain;
    private Integer remainNum;
    private Integer availableNum;
    private Integer num;
}