package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class PaymentTypeVO {

    private String paymentTypeId;
    private String paymentTypeName;
    private Integer ignoreOrderGetItem;
    private Integer isCustom;
    private Integer isIncome;
    private Integer isEnable;
    private Integer bizType;
    private Integer groupType;
}
