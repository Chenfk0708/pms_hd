package com.jeez.zp.crm.dto.request;

import lombok.Data;

@Data
public class CustomerPageRequest {

    private String campId;
    private Integer current;
    private Integer page;
    private Integer pageNum;
    private Integer pageSize;
    private String keyword;
}
