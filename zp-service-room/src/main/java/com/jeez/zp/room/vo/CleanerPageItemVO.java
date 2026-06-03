package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class CleanerPageItemVO {

    private String id;
    private String cleanerId;
    private String name;
    private String cleanerName;
    private String mobile;
    private String poiId;
    private String poiName;
    private String storeName;
    private String status;
    private String workStatus;
    private String statusText;
    private String role;
    private String roleName;
    private List<String> roomScope;
    private List<String> roomScopes;
    private Integer todayTasks;
    private Integer todayTaskNum;
    private Integer completedTasks;
    private Integer completedTaskNum;
    private Integer overdueTasks;
    private Integer overdueTaskNum;
    private Integer serviceScore;
    private String rating;
    private String lastTaskAt;
    private String lastTaskTime;
}
