package com.jeez.zp.crm.dto.request;

import lombok.Data;

@Data
public class ScrmWechatQueryRequest {

    private String campId;
    private String poiId;
    private String statDate;
    private String startDate;
    private String endDate;
    private String channel;
    private String status;
    private String keyword;
    private Integer page;
    private Integer pageNum;
    private Integer current;
    private Integer pageSize;
}
