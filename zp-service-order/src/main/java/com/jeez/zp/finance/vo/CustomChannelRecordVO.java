package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class CustomChannelRecordVO {
    private String id;
    private String name;
    private String code;
    private String color;
    private String colorName;
    private Boolean enabled;
    private String updatedAt;
    private String operator;
    private String note;
}
