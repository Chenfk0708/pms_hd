package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class PaymentWayVO {
    private String paymentWayId;
    private String paymentWayName;
    private String paymentWayCode;
    private String wayType;
    private Integer sortNo;
    private Integer isCustom;
    private Integer isEnable;
}
