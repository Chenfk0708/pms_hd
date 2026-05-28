package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class ComprehensiveMonthlyReportPageRequest {

    private String campId;
    private String startDate;
    private String endDate;
    private Integer page;
    private Integer pageNum;
    private Integer pageSize;
    private Integer current;
}
