package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class CleanStatisticsDetailRowVO {

    private String id;
    private String cleanDate;
    private String roomName;
    private String cleanerName;
    private String cleanType;
    private Long fee;
    private String status;
}
