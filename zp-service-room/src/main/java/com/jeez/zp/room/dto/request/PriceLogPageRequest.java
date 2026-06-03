package com.jeez.zp.room.dto.request;

import lombok.Data;

@Data
public class PriceLogPageRequest {

    private String campId;
    private String keyword;
    private String adjustType;
    private String channelId;
    private String adjustmentStart;
    private String adjustmentEnd;
    private String operationStart;
    private String operationEnd;
    private String operator;
    private Integer page;
    private Integer pageNum;
    private Integer pageSize;
}
