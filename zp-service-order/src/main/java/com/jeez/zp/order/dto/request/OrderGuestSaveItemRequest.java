package com.jeez.zp.order.dto.request;

import lombok.Data;

@Data
public class OrderGuestSaveItemRequest {

    private String guestId;
    private String guestName;
    private String guestMobile;
    private String guestIdCardType;
    private String guestIdCard;
    private String guestType;
}
