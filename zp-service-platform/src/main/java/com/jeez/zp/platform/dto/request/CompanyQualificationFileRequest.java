package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class CompanyQualificationFileRequest {

    private String id;
    private String name;
    private String kind;
    private String uploadedAt;
    private String sizeLabel;
}
