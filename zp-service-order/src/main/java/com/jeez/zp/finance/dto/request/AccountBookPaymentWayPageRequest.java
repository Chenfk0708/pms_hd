package com.jeez.zp.finance.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class AccountBookPaymentWayPageRequest {
    private String campId;
    private String beginTime;
    private String endTime;
    private List<String> poiIds;
    private Integer pageNum;
    private Integer pageSize;
    private String exportExcelMenuId;
}
