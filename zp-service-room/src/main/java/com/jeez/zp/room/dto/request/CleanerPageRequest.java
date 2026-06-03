package com.jeez.zp.room.dto.request;

import lombok.Data;

@Data
public class CleanerPageRequest {

    private String campId;
    private String poiId;
    private String keyword;
    private String status;
    private String serviceDate;
    private Integer pageNum;
    private Integer pageSize;
}
