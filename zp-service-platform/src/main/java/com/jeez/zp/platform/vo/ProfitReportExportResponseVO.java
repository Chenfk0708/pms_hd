package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class ProfitReportExportResponseVO {

    private String taskId;
    private String fileName;
    private String contentType;
    private String downloadUrl;
    private Integer total;
    private List<ProfitReportRowVO> rows;
}
