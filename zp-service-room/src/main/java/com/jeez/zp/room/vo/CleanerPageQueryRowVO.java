package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class CleanerPageQueryRowVO {

    private String cleanerId;
    private String cleanerName;
    private String mobile;
    private Integer rawStatus;
    private String poiId;
    private String poiName;
    private String roomScopesText;
    private Integer todayTaskNum;
    private Integer completedTaskNum;
    private Integer overdueTaskNum;
    private String lastTaskTime;
}
