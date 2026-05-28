package com.jeez.zp.room.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoomSituationOrderRowVO {

    private String orderId;
    private String roomId;
    private String roomCategoryId;
    private String orderStatus;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;
}
