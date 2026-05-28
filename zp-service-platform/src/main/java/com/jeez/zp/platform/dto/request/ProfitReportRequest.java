package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class ProfitReportRequest {

    private String campId;
    private Integer current;
    private Integer pageNum;
    private Integer pageSize;
    private String startDate;
    private String endDate;
    private Boolean breakTemp;
    private Integer isCleanCost;
    private String poiId;
    private String roomCategoryId;
    private String roomCategoryGroupId;
    private String channelId;
    private String roomId;
    private String exportExcelMenuId;
}
