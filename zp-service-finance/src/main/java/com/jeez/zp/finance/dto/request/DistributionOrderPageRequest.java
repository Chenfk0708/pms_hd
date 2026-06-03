package com.jeez.zp.finance.dto.request;

import lombok.Data;

@Data
public class DistributionOrderPageRequest {
    private String campId;
    private Integer pageNum;
    private Integer pageSize;
    private Integer current;
    private String bookingStartDate;
    private String bookingEndDate;
    private String keyword;
    private String settlementStatus;
    private String settledState;
    private Boolean breakTemp;
}
