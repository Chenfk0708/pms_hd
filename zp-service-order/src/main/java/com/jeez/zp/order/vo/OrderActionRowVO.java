package com.jeez.zp.order.vo;

import lombok.Data;

@Data
public class OrderActionRowVO {

    private Long orderId;
    private Long campId;
    private String status;
    private String guestName;
    private String guestMobile;
}
