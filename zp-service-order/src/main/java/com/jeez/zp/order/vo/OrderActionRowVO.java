package com.jeez.zp.order.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderActionRowVO {

    private Long orderId;
    private Long campId;
    private Long poiId;
    private Long roomCategoryId;
    private Long roomId;
    private String status;
    private String orderType;
    private String poiName;
    private String roomCategoryName;
    private String roomName;
    private String guestName;
    private String guestMobile;
    private Long totalPriceCent;
    private Long totalPayPriceCent;
    private Long commissionPriceCent;
    private String paymentStatus;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String roomSnapshotJson;
    private String remark;
    private LocalDateTime guestRegisteredAt;
    private LocalDateTime checkedOutAt;
}
