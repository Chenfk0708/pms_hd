package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CleanTaskStatisticsRequest {

    private String campId;
    private String storeId;
    private List<String> roomIds;
    private List<String> cleanerIds;
    private Long cleanStartTime;
    private Long cleanEndTime;
    private Integer pageNum;
    private Integer pageSize;
}
