package com.jeez.zp.finance.dto.request;

import lombok.Data;

@Data
public class FullMarketingCommissionPageRequest {
    private String campId;
    private Integer pageNum;
    private Integer pageSize;
    private String type;
    private String keyword;
}
