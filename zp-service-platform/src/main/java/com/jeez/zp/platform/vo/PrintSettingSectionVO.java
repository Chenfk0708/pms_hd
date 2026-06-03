package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class PrintSettingSectionVO {

    private String key;
    private String title;
    private String ariaLabel;
    private String paperType;
    private List<PrintDocumentOptionVO> paperOptions;
    private String selectedDocument;
    private List<PrintDocumentOptionVO> documentOptions;
    private String customText;
    private String placeholder;
}
