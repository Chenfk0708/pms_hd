package com.jeez.zp.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiGlobalStrongReminderRowVO {

    private String orderId;
    private Long campId;
    private String outOrderNo;
    private String orderNo;
    private String guestName;
    private String roomName;
    private String roomCategoryName;
    private Long channelId;
    private String channelName;
    private String orderStatus;
    private LocalDateTime startAt;
    private LocalDateTime createdAt;
}
