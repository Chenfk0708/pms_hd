package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class SmsTemplateMsgConfigItemVO {

    private String smsTemplateMsgConfigId;
    private Integer type;
    private String name;
    private String sendTimeText;
    private String passContent;
    private String auditContent;
    private Integer sendStatus;
    private Integer isEnabled;
    private Integer signPosition;
    private String signName;
    private String fixedContentPrefix;
    private String fixedContentSuffix;
    private String passCustomContent;
    private String auditCustomContent;
    private String exampleParam;
    private Integer auditStatus;
}
