package com.jeez.zp.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkspaceOrderListRowVO {

    private String orderId;
    private String channelName;
    private String guestName;
    private String guestMobile;
    private String roomCategoryName;
    private String roomName;
    private String orderStatus;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer dayNum;
    private LocalDateTime createdAt;
}
