package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class CleanerPageSummaryVO {

    private Integer total;
    private Integer onDuty;
    private Integer offDuty;
    private Integer leave;
    private Integer todayTasks;
    private Integer completedTasks;
    private Integer overdueTasks;
}
