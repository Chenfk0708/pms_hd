package com.jeez.zp.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoomStatusOperationLogQueryRowVO {

    private String roomStatusOperationLogId;
    private String roomCategoryName;
    private String roomName;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String sourceType;
    private String orderStatus;
    private String paymentStatus;
    private String channelId;
    private String channelName;
    private String channelRoomCategoryProductName;
    private String userName;
    private LocalDateTime createTime;
    private String remark;
}
