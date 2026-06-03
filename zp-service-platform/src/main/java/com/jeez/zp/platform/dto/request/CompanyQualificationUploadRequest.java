package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class CompanyQualificationUploadRequest {

    private String campId;
    private String target;
    private String fileName;
    private String kind;
    private String sizeLabel;
    private String mediaId;
}
