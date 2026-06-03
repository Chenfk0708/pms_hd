package com.jeez.zp.finance.dto.request;

import lombok.Data;

@Data
public class SocialOverviewRequest {
    private String bizDate;
    private String campId;
    private String projectId;
    private String channelStatus;
    private String keyword;
    private Integer page;
    private Integer pageSize;
}
