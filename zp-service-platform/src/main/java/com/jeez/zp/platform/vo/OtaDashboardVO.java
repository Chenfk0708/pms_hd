package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class OtaDashboardVO {

    private List<OtaOptionVO> stores;
    private List<OtaOptionVO> dimensions;
    private List<OtaMetricVO> metrics;
    private List<OtaChannelVO> connectedChannels;
    private List<OtaChannelVO> pendingChannels;
    private List<OtaReminderVO> reminders;
    private List<OtaQuickLinkVO> quickLinks;
    private String updatedAt;
    private String provider;
    private String traceId;
    private OtaDashboardRequestVO request;
}
