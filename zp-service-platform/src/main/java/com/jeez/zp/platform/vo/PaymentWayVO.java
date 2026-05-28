package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class PaymentWayVO {

    private String paymentWayId;
    private String paymentWayName;
    private Integer isCustom;
    private Integer isEnable;
}
