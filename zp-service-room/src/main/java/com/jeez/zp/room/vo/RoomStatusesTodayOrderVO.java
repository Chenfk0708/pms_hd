package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class RoomStatusesTodayOrderVO {

    private String orderId;
    private String channelId;
    private String channelName;
    private String guestName;
    private String guestMobile;
    private String status;
    private String remark;
    private Long checkInDate;
    private Long checkOutDate;
    private Long totalPriceCent;
    private Long totalPayPriceCent;
}
