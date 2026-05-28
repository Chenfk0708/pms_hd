package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ForwardRoomStatusRequest {

    private String campId;
    private String startDate;
    private String endDate;
    private List<String> poiIds;
    private Integer current;
    private Integer pageNum;
    private Integer pageSize;
}
