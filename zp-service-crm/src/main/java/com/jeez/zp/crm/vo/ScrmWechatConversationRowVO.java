package com.jeez.zp.crm.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScrmWechatConversationRowVO {

    private Long orderId;
    private String orderNo;
    private String guestName;
    private String guestMobile;
    private String channelName;
    private String orderStatus;
    private String roomCategoryName;
    private String roomName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;
    private Long totalPayPriceCent;
    private String remark;
    private String assignee;
}
