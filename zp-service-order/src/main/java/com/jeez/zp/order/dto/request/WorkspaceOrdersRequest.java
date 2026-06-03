package com.jeez.zp.order.dto.request;

import lombok.Data;

@Data
public class WorkspaceOrdersRequest {

    private String campId;
    private Integer current;
    private Integer page;
    private Integer pageNum;
    private Integer pageSize;
    private String orderType;
    private String keyword;
}
