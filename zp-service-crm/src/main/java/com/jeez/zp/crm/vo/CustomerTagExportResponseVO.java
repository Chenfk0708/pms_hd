package com.jeez.zp.crm.vo;

import lombok.Data;

import java.util.List;

@Data
public class CustomerTagExportResponseVO {

    private String taskId;
    private String fileName;
    private String contentType;
    private Long total;
    private List<CustomerTagGroupVO> rows;
}
