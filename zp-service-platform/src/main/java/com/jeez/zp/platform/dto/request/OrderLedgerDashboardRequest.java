package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class OrderLedgerDashboardRequest {

    private String campId;
    private Integer pageNum;
    private Integer pageSize;
    private String beginTime;
    private String endTime;
    private List<String> paymentTypeIds;
    private List<String> paymentWayIds;
    private List<String> roomIds;
    private List<String> poiIds;
    private String keyword;
    private Integer isIncome;
    private Integer type;
}
