package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class CleanStatisticsDashboardResponseVO {

    private CleanStatisticsPayloadVO statistics;
    private List<CleanTaskOptionVO> stores;
    private List<CleanTaskOptionVO> rooms;
    private List<CleanTaskOptionVO> cleaners;
}
