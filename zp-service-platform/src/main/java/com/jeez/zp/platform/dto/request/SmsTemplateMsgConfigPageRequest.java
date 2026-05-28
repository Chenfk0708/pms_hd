package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class SmsTemplateMsgConfigPageRequest {

    private String campId;
    private Integer sendType;
    private Integer pageNum;
    private Integer pageSize;
}
