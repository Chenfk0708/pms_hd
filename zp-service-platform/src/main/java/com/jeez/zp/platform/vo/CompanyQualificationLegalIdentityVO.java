package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class CompanyQualificationLegalIdentityVO {

    private String documentType;
    private String documentNumber;
    private List<CompanyQualificationLegalPhotoVO> photos;
}
