package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CleanLogPageRequest {

    private String campId;
    private Integer pageNum;
    private Integer pageSize;
    private String poiId;
    private List<String> roomId;
    private String operatorId;
    private Long operatorStartTime;
    private Long operatorEndTime;
}
