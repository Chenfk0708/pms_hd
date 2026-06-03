package com.jeez.zp.crm.vo;

import lombok.Data;

@Data
public class CustomerItemVO {

    private String customerId;
    private String name;
    private String mobile;
    private String profileJson;
    private String lastActiveAt;
    private Integer status;
}
