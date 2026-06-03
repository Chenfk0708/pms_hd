package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class PrintSettingsSaveRequest {

    private String campId;
    private String section;
    private String paperType;
    private String selectedDocument;
    private String customText;
}
