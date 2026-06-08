package com.jeez.zp.finance.dto.request;

import lombok.Data;

@Data
public class PaymentSettingMutationRequest {
    private String campId;
    private String methodId;
    private String name;
    private String status;
    private String direction;
    private String exportAt;
}
