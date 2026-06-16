package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class SalesReportRequest {

    private String campId;
    private String startDate;
    private String endDate;
    private Integer queryType;
    private Integer pageNum;
    private Integer current;
    private Integer pageSize;
    private List<String> poiIds;
    private List<String> roomCategoryIds;
    private List<String> roomCategoryGroupIds;
    private List<String> channelIds;
    private List<String> roomIds;
}
