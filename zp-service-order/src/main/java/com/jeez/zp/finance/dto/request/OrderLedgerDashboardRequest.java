package com.jeez.zp.finance.dto.request;

import lombok.Data;

@Data
public class OrderLedgerDashboardRequest {
    private String campId;
    private Integer pageNum;
    private Integer pageSize;
    private String beginTime;
    private String endTime;
    private Integer isIncome;
    private String roomCategoryId;
}
