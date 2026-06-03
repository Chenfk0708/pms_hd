package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class CleanStatisticsPayloadVO {

    private List<CleanTaskStatisticsRowVO> list;
    private List<CleanStatisticsDetailRowVO> detailList;
    private List<CleanStatisticsMetricVO> metrics;
    private List<CleanStatisticsTodoVO> todos;
    private CleanTaskPaginationVO pagination;
}
