package com.jeez.zp.crm.vo;

import lombok.Data;

import java.util.List;

@Data
public class ScrmSidebarExportResponseVO {

    private String taskId;
    private String fileName;
    private String contentType;
    private Integer total;
    private List<ScrmSidebarConversationVO> rows;
}
