package com.jeez.zp.finance.vo;

import lombok.Data;

import java.util.Map;

@Data
public class FullMarketingMetricVO {
    private Double turnover;
    private Double commission;
    private Integer orderCount;
    private Map<String, Object> requestEcho;
}
