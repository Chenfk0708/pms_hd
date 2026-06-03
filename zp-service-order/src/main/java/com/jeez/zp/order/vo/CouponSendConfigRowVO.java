package com.jeez.zp.order.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CouponSendConfigRowVO {

    private String id;
    private String couponId;
    private String couponName;
    private String sendType;
    private String sendMethod;
    private Integer sentCount;
    private String createdAt;
    private String recordText;
    private Integer status;
    private LocalDateTime createdAtValue;
}
