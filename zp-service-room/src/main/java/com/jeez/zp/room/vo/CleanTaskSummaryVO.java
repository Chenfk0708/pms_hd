package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class CleanTaskSummaryVO {

    private Integer total;
    private Integer pendingAssign;
    private Integer pendingClean;
    private Integer cleaning;
    private Integer done;
    private Integer overdue;
}
