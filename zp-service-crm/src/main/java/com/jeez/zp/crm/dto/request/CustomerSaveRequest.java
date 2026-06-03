package com.jeez.zp.crm.dto.request;

import lombok.Data;

@Data
public class CustomerSaveRequest {

    private String customerId;
    private String campId;
    private String name;
    private String mobile;
    private String profileJson;
}
