package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class CompanyQualificationLegalPhotoVO {

    private String id;
    private String label;
    private List<CompanyQualificationFileVO> files;
}
