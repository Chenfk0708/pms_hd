package com.jeez.zp.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProfitReportCleanTaskRowVO {

    private String taskId;
    private String taskType;
    private String taskStatus;
    private LocalDateTime deadlineAt;
}
