package com.jeez.zp.order.vo;

import lombok.Data;

import java.util.List;

@Data
public class OrderDetailAggregateVO {

    private String orderId;
    private String orderNo;
    private String outOrderId;
    private String orderType;
    private String status;
    private String statusName;
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
    private String checkInTime;
    private String checkOutTime;
    private Integer dayNum;
    private Long totalPrice;
    private Long totalPayPrice;
    private Long refundPrice;
    private Long commissionPrice;
    private Long debtPrice;
    private String paymentWayId;
    private String paymentWayName;
    private String remark;
    private String createdAt;
    private List<OrderGuestVO> guests;
    private List<OrderPaymentRecordVO> paymentRecords;
}
