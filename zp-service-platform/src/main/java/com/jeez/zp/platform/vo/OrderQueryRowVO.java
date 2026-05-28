package com.jeez.zp.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderQueryRowVO {

    private String orderId;
    private String outOrderId;
    private String channelId;
    private String channelName;
    private String orderChannelName;
    private String guestName;
    private String guestMobile;
    private String poiId;
    private String roomCategoryId;
    private String status;
    private String paymentStatus;
    private String paymentWayId;
    private String sourceType;
    private Long totalPriceCent;
    private Long totalPayPriceCent;
    private Long refundPriceCent;
    private Long commissionPriceCent;
    private Long sellingPriceCent;
    private Long settlementAmountCent;
    private Integer count;
    private Integer roomCategoryType;
    private String categoryId;
    private String categoryName;
    private String paymentWayName;
    private String confirmNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String poiName;
    private String roomCategoryName;
    private String roomCategoryProductName;
    private String roomName;
}
