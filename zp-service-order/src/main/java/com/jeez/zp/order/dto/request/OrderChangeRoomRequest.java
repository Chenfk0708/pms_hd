package com.jeez.zp.order.dto.request;

import lombok.Data;

@Data
public class OrderChangeRoomRequest {

    private String campId;
    private String roomId;
    private String reason;
}
