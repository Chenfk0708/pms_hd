package com.jeez.zp.finance.dto.request;

import lombok.Data;

@Data
public class FullMarketingReportRequest {
    private String campId;
    private String startDate;
    private String endDate;
    private String type;
    private Integer pageNum;
    private Integer pageSize;
}
