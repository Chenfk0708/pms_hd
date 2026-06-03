package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class PsbLogRetryRequest {

    private String campId;
    private String id;
    private String orderNo;
}
