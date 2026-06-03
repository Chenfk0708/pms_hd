package com.jeez.zp.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CompanyQualificationAssetRowVO {

    private Long assetId;
    private Long qualificationId;
    private Long campId;
    private String assetType;
    private String fileKind;
    private Long mediaId;
    private String fileName;
    private LocalDateTime uploadedAt;
    private String mediaName;
    private String url;
    private Long sizeBytes;
}
