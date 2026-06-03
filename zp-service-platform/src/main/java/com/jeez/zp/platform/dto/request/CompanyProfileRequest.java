package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CompanyProfileRequest {

    private String name;
    private String type;
    private String phone;
    private String city;
    private String address;
    private List<CompanyQualificationFileRequest> images;
}
