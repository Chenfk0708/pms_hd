package com.jeez.zp.room.dto.request;

import lombok.Data;

@Data
public class CleanTaskActionRequest {

    private String campId;
    private String taskId;
    private String cleanerId;
    private String remark;
}
