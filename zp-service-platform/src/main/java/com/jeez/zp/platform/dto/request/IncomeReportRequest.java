package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class IncomeReportRequest {

    private String campId;
    private String startDate;
    private String endDate;
    private String poiId;
    private String roomCategoryId;
    private String roomCategoryGroupId;
    private String channelId;
    private String roomId;
    private Integer queryType;
    private Integer pageNum;
    private Integer current;
    private Integer pageSize;
}
