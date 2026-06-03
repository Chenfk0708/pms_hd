package com.jeez.zp.order.vo;

import lombok.Data;

@Data
public class OrderActionResponseVO {

    private String orderId;
    private String status;
    private Integer guestCount;
    private String message;
}
