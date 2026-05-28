package com.jeez.zp.room.dto.request;

import lombok.Data;

@Data
public class CleanSettingsBootstrapRequest {

    private String campId;
    private String businessDate;
    private String storeId;
    private String projectId;
    private String status;
    private Integer page;
    private Integer pageSize;
}
