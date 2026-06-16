package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class CleanTaskActionRowVO {

    private Long cleanTaskId;
    private Long campId;
    private Long poiId;
    private Long roomId;
    private Long roomCategoryId;
    private Long cleanStaffId;
    private String taskType;
    private String taskStatus;
    private String remark;
}
