package com.jeez.zp.room.dto.request;

import lombok.Data;

@Data
public class CleanTaskCreateRequest {

    private String campId;
    private String poiId;
    private String roomId;
    private String cleanerId;
    private String cleanType;
    private String cleanStatus;
    private String deadlineAt;
    private String cleanTime;
    private String deadline;
    private String remark;
}
