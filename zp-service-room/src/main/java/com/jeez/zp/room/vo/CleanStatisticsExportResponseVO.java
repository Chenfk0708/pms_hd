package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class CleanStatisticsExportResponseVO {

    private String taskId;
    private String fileName;
    private String contentType;
    private String downloadUrl;
    private Integer total;
    private List<CleanStatisticsDetailRowVO> rows;
}
