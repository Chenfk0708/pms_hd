package com.jeez.zp.order.dto.request;

import lombok.Data;

@Data
public class OrderCreateKeyValueItemRequest {

    private String id;
    private String text;
    private String date;
    private String content;
}
