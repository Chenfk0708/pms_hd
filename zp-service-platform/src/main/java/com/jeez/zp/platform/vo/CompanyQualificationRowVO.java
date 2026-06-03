package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class CompanyQualificationRowVO {

    private Long qualificationId;
    private Long campId;
    private String documentType;
    private String documentNumber;
    private String legalPersonName;
    private String legalPersonIdNumber;
}
