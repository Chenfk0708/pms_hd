package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class ApiKeyRecordVO {

    private String appId;
    private String accessKeyId;
    private String secretKeyPreview;
    private String createdAt;
    private String lastUsedAt;
    private String rotationTip;
    private String status;
    private List<String> scopes;
}
