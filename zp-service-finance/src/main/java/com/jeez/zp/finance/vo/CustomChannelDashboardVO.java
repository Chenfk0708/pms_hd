package com.jeez.zp.finance.vo;

import lombok.Data;

import java.util.List;

@Data
public class CustomChannelDashboardVO {
    private List<SystemChannelVO> systemChannels;
    private List<CustomChannelRecordVO> customChannels;
}
