package com.jeez.zp.order.dto.request;

import lombok.Data;

@Data
public class OrderCreateRoomItemRequest {

    private String roomId;
    private String roomType;
    private String roomName;
    private String dateRange;
    private String checkInDate;
    private String checkOutDate;
    private Long price;
    private Integer quantity;
    private Integer guests;
}
