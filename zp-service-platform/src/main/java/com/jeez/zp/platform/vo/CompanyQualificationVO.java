package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class CompanyQualificationVO {

    private String provider;
    private String state;
    private CompanyQualificationProfileVO profile;
    private List<CompanyQualificationFieldVO> fields;
    private List<String> cityOptions;
    private List<CompanyQualificationDocumentSectionVO> businessLicenses;
    private CompanyQualificationLegalIdentityVO legalIdentity;
}
