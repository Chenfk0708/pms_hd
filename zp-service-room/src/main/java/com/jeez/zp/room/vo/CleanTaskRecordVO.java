package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class CleanTaskRecordVO {

    private String taskId;
    private String taskNo;
    private String roomName;
    private String poiName;
    private String cleanType;
    private String cleanStatus;
    private String cleanerId;
    private String cleanerName;
    private String cleanDate;
    private String planTime;
    private String deadline;
    private String sourceOrderNo;
    private String guestName;
    private String remark;
    private Integer progress;
    private String priority;
}
