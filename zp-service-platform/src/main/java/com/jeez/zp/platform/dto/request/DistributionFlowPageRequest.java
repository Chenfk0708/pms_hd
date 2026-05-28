package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class DistributionFlowPageRequest {

    private String campId;
    private Integer pageNum;
    private Integer current;
    private Integer pageSize;
    private String bookingStartDate;
    private String bookingEndDate;
    private String keyword;
    private Boolean breakTemp;
    private String settledState;
}
