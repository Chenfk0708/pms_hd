package com.jeez.zp.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkspaceDashboardRoomRowVO {

    private String roomId;
    private String cleanStatus;
    private String lockStatus;
    private String orderId;
    private String orderStatus;
    private LocalDateTime checkInAt;
    private LocalDateTime checkOutAt;
}
