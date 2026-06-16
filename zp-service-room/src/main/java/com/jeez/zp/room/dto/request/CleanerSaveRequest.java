package com.jeez.zp.room.dto.request;

import lombok.Data;

@Data
public class CleanerSaveRequest {

    private String campId;
    private String poiId;
    private String name;
    private String mobile;
    private String roomScopeText;
    private String status;
}
