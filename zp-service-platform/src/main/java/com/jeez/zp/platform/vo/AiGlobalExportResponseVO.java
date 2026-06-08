package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class AiGlobalExportResponseVO {

    private String taskId;
    private String fileName;
    private String contentType;
    private String downloadUrl;
    private Integer total;
}
