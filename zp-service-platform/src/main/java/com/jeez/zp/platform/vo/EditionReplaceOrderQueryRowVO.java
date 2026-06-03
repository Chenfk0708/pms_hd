package com.jeez.zp.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EditionReplaceOrderQueryRowVO {

    private Long orderId;
    private String orderNo;
    private String outOrderNo;
    private String channelName;
    private String roomCategoryName;
    private String roomName;
    private String guestName;
    private String guestMobile;
    private String orderStatus;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;
    private Long settlementAmountCent;
    private Long replaceAmountCent;
    private String remark;
}
