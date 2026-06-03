package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class CompanyInfoVO {

    private String name;
    private String type;
    private String phone;
    private String city;
    private String address;
    private List<CompanyInfoImageVO> images;
}
