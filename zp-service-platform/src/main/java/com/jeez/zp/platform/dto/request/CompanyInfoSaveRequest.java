package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class CompanyInfoSaveRequest {

    private String campId;
    private CompanyProfileRequest profile;
}
