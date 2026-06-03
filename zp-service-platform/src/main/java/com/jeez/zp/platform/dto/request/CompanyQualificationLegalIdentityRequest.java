package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class CompanyQualificationLegalIdentityRequest {

    private String documentType;
    private String documentNumber;
}
