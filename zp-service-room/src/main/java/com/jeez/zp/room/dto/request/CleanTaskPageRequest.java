package com.jeez.zp.room.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CleanTaskPageRequest {

    private String campId;
    private String poiId;
    private String cleanTime;
    private String roomId;
    private String cleanType;
    private String cleanStatus;
    private List<String> cleanerIds;
    private Integer pageNum;
    private Integer pageSize;
}
