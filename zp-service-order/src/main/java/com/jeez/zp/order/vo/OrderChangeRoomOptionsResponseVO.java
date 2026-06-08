package com.jeez.zp.order.vo;

import lombok.Data;

import java.util.List;

@Data
public class OrderChangeRoomOptionsResponseVO {

    private String orderId;
    private String roomId;
    private String roomName;
    private String roomCategoryId;
    private String roomCategoryName;
    private List<OrderChangeRoomOptionVO> rooms;
}
