package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class CustomChannelRowVO {
    private String id;
    private String name;
    private String code;
    private String color;
    private String colorName;
    private Integer status;
    private String updatedAt;
    private String operator;
    private String note;
}
