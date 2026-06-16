package com.jeez.zp.order.vo;

import lombok.Data;

@Data
public class ChannelCallbackOrderRowVO {

    private String orderId;
    private String outOrderNo;
    private String orderNo;
    private String status;
    private String paymentStatus;
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
    private Long totalPrice;
    private Long totalPayPrice;
    private Long commissionPrice;
    private String createdAt;
}
