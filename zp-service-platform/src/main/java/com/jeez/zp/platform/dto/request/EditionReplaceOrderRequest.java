package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class EditionReplaceOrderRequest {

    private String campId;
    private Long receiverStartTime;
    private Long receiverEndTime;
    private Integer pageNum;
    private Integer current;
    private Integer pageSize;
}
