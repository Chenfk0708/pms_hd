package com.jeez.zp.order.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StrongReminderRowVO {

    private String orderId;
    private String campId;
    private String orderNo;
    private String status;
    private String paymentStatus;
    private String guestName;
    private String guestMobile;
    private String roomCategoryName;
    private String roomName;
    private String channelId;
    private String channelName;
    private LocalDateTime startAt;
    private LocalDateTime createdAt;
}
