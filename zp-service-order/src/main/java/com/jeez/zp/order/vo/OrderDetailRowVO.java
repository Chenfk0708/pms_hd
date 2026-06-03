package com.jeez.zp.order.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderDetailRowVO {

    private String orderId;
    private String orderNo;
    private String outOrderId;
    private String orderType;
    private String status;
    private String paymentStatus;
    private String channelId;
    private String channelName;
    private String guestName;
    private String guestMobile;
    private String poiId;
    private String poiName;
    private String roomCategoryId;
    private String roomCategoryName;
    private String roomId;
    private String roomName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer dayNum;
    private Long totalPriceCent;
    private Long totalPayPriceCent;
    private Long refundPriceCent;
    private Long commissionPriceCent;
    private String paymentWayId;
    private String paymentWayName;
    private String remark;
    private LocalDateTime createdAt;
}
