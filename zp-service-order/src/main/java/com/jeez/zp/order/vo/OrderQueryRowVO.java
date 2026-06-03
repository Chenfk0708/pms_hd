package com.jeez.zp.order.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderQueryRowVO {

    private String orderId;
    private String outOrderId;
    private String channelId;
    private String channelName;
    private String orderChannelName;
    private String orderType;
    private String guestName;
    private String guestMobile;
    private String poiId;
    private String roomCategoryId;
    private String roomId;
    private String status;
    private String paymentStatus;
    private String paymentWayId;
    private String paymentWayName;
    private Long totalPriceCent;
    private Long totalPayPriceCent;
    private Long refundPriceCent;
    private Long commissionPriceCent;
    private Long depositPriceCent;
    private Long otherPriceCent;
    private String confirmNo;
    private LocalDateTime createdAt;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String poiName;
    private String roomCategoryName;
    private String roomCategoryProductName;
    private String roomName;
    private String contractNo;
    private Long nextPaymentAmountCent;
    private LocalDateTime nextPaymentDate;
    private String paymentCycle;
}
