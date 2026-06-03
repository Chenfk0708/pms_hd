package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class CompanyQualificationDocumentSectionVO {

    private String id;
    private String title;
    private List<String> links;
    private String hint;
    private String uploadLabel;
    private String kind;
    private Integer maxFiles;
    private List<CompanyQualificationFileVO> files;
}
