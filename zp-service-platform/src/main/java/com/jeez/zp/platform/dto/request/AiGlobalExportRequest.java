package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class AiGlobalExportRequest {

    private String campId;
    private String channel;
    private String attention;
    private String roomKeyword;
}
