package com.jeez.zp.order.vo;

import lombok.Data;

@Data
public class OrderActionResponseVO {

    private String orderId;
    private String status;
    private Integer guestCount;
    private String roomId;
    private String roomName;
    private String roomCategoryId;
    private String roomCategoryName;
    private String guestRegisteredAt;
    private String checkedOutAt;
    private String message;
}
