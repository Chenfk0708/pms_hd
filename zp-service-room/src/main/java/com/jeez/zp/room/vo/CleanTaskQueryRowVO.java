package com.jeez.zp.room.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CleanTaskQueryRowVO {

    private String taskId;
    private String poiName;
    private String roomCategoryName;
    private String roomName;
    private String cleanerId;
    private String cleanerName;
    private String taskType;
    private String taskStatus;
    private String remark;
    private LocalDateTime deadlineAt;
}
