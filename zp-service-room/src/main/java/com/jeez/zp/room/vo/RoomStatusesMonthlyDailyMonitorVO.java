package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class RoomStatusesMonthlyDailyMonitorVO {

    private String date;
    private String remain;
    private Integer remainNum;
    private Integer availableNum;
    private Integer num;
}