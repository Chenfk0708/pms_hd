package com.jeez.zp.finance.dto.request;

import lombok.Data;

@Data
public class PaymentTypeCreateRequest {
    private String campId;
    private Integer groupType;
    private String groupName;
    private String paymentTypeName;
    private Integer isIncome;
    private Integer bizType;
    private Integer ignoreOrderGetItem;
}
