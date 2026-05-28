package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class CleanTaskDashboardResponseVO {

    private List<CleanTaskOptionVO> stores;
    private List<CleanTaskOptionVO> rooms;
    private List<CleanTaskOptionVO> cleanTypes;
    private List<CleanTaskOptionVO> statuses;
    private List<CleanTaskOptionVO> cleaners;
    private CleanTaskSummaryVO summary;
    private List<CleanTaskRecordVO> list;
    private CleanTaskPaginationVO pagination;
}
