package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class CleanTaskExportResponseVO {

    private String fileName;
    private String contentType;
    private Integer total;
    private List<CleanTaskRecordVO> rows;
}
