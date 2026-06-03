package com.jeez.zp.crm.vo;

import lombok.Data;

@Data
public class WechatKfSummaryVO {

    private Integer todaySessions;
    private Integer pendingSessions;
    private Integer averageReplySeconds;
    private Integer conversionLeads;
    private String responseRate;
}
