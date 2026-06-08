package com.jeez.zp.finance.vo;

import lombok.Data;

import java.util.List;

@Data
public class SocialChannelVO {
    private String id;
    private String name;
    private String status;
    private String relation;
    private List<String> support;
    private String action;
    private String accent;
    private String conversionRate;
    private Integer roomTypeCount;
    private Integer linkedRoomTypeCount;
    private Integer dailyOrders;
    private List<String> pendingTasks;
}
