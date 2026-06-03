package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class CleanTaskActionResponseVO {

    private String taskId;
    private String taskNo;
    private String cleanStatus;
    private Integer notifiedCount;
    private List<String> taskIds;
    private String message;
}
