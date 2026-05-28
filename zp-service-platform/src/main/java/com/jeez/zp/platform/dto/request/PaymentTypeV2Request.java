package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class PaymentTypeV2Request {

    private String campId;
    private List<Integer> bizTypes;
    private Integer isEnable;
}
