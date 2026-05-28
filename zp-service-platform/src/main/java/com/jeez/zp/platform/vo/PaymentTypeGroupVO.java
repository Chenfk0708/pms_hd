package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class PaymentTypeGroupVO {

    private Integer groupType;
    private String groupTypeName;
    private List<PaymentTypeVO> paymentTypes;
}
