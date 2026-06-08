package com.jeez.zp.finance.dto.request;

import lombok.Data;

@Data
public class ShiftWorkReportPageRequest {
    private String campId;
    private Integer pageNum;
    private Integer pageSize;
    private String startDate;
    private String endDate;
    private String poiId;
    private String storeId;
    private String handoverUserId;
    private String receiverUserId;
}
