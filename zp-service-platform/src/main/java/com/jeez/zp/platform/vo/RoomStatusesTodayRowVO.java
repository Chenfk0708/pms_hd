package com.jeez.zp.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoomStatusesTodayRowVO {

    private String roomId;
    private String roomName;
    private String poiId;
    private String roomCategoryId;
    private String roomCategoryName;
    private Integer roomCategorySeq;
    private String roomCategoryGroupId;
    private String floorId;
    private String floorName;
    private Integer floorSeq;
    private String cleanStatus;
    private String saleType;
    private Integer roomSeq;

    private String orderId;
    private String orderStatus;
    private String paymentStatus;
    private String guestName;
    private String guestMobile;
    private String orderRemark;
    private String channelId;
    private String channelName;
    private LocalDateTime checkInAt;
    private LocalDateTime checkOutAt;
    private Long totalPriceCent;
    private Long totalPayPriceCent;
    private String sourceType;
}
