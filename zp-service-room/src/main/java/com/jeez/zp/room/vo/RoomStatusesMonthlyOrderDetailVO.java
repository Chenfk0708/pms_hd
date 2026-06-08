package com.jeez.zp.room.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomStatusesMonthlyOrderDetailVO {

    private String roomCategoryId;
    private String roomId;
    private String date;
    private String guestName;
    private String channelName;
    private BigDecimal roomFee;
    private BigDecimal totalIncome;
    private String orderType;
    private String startAt;
    private String endAt;
    private String bookingAt;
    private String guestRegisteredAt;
    private String checkedOutAt;
    private String stayRange;
    private String phone;
    private String remark;
    private String orderId;
    private String id;
    private Boolean hasRemark;
    private String liveStatusName;
    private String statusName;
    private String checkInDate;
    private String checkOutDate;
}
