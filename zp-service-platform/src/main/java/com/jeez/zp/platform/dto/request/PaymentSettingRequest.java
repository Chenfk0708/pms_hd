package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class PaymentSettingRequest {

    private String campId;
    private String methodId;
    private Boolean includeDisabled;
}
