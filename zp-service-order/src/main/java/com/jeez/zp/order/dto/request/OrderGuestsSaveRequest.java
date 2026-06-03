package com.jeez.zp.order.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class OrderGuestsSaveRequest {

    private String campId;
    private List<OrderGuestSaveItemRequest> guests;
}
