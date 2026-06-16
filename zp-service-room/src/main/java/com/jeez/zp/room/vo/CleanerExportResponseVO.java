package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class CleanerExportResponseVO {

    private String taskId;
    private String fileName;
    private String contentType;
    private Integer total;
    private List<CleanerPageItemVO> rows;
}
