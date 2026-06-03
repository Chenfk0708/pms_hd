package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class CompanyQualificationSaveRequest {

    private String campId;
    private CompanyProfileRequest profile;
    private CompanyQualificationLegalIdentityRequest legalIdentity;
    private String legalPersonName;
    private String legalPersonIdNumber;
}
