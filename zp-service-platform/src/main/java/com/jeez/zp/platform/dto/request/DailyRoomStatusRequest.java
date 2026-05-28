package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class DailyRoomStatusRequest {

    private String campId;
    private String date;
    private List<String> poiIds;
    private Integer current;
    private Integer pageNum;
    private Integer pageSize;
}
