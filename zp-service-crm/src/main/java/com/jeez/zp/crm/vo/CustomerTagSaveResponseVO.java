package com.jeez.zp.crm.vo;

import lombok.Data;

import java.util.List;

@Data
public class CustomerTagSaveResponseVO {

    private String tagGroupId;
    private List<String> tagNames;
    private String message;
}
