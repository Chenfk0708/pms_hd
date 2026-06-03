package com.jeez.zp.crm.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CustomerTagSaveRequest {

    private String campId;
    private String tagGroupId;
    private String tagGroupName;
    private List<String> tagNames;
    private String source;
}
