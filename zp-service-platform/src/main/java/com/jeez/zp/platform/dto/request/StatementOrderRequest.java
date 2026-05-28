package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class StatementOrderRequest {

    private String campId;
    private List<String> poiIds;
    private String bookingStartDate;
    private String bookingEndDate;
    private Integer current;
    private Integer pageNum;
    private Integer pageSize;
    private Boolean breakTemp;
    private String exportExcelMenuId;
}
