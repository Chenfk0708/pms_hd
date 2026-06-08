package com.jeez.zp.finance.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SocialOverviewVO {
    private SocialFilterOptionsVO filterOptions;
    private List<SocialMetricVO> metrics;
    private List<SocialChannelVO> channels;
    private List<SocialTrendPointVO> trend;
    private List<SocialTodoVO> todos;
    private SocialAccountsVO accounts;
    private List<SocialQuickLinkVO> quickLinks;
    private String updatedAt;
    private Map<String, Object> requestEcho;
}
