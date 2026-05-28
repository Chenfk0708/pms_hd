package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class DailyRoomStatusRowVO {

    private String roomCategoryId;
    private String roomCategoryName;
    private Integer availabilityCount;
    private Integer openRoomCount;
    private Integer roomSaleCount;
    private Integer closeRoomCount;
    private Integer userBusyNum;
    private Integer userBusyRetainNum;
    private Integer userBusyRepairNum;
    private Integer mainViceRelNum;
    private Integer totalVacantRoomCount;
    private Integer preComeNum;
    private Integer liveNum;
    private Integer preLeaveNum;
    private Integer cleanNum;
    private Integer dirtyNum;
}
