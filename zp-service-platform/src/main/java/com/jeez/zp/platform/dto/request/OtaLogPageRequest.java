package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class OtaLogPageRequest {

    private String campId;
    private String channelId;
    private String keyword;
    private String operator;
    private String operationType;
    private String operationStatus;
    private Integer page;
    private Integer pageSize;
}
