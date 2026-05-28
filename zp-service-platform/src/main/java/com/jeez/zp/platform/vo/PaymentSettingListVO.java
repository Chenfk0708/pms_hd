package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class PaymentSettingListVO {

    private List<PaymentMethodVO> methods;
    private String notice;
}
