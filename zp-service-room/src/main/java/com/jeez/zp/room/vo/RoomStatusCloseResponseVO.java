package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class RoomStatusCloseResponseVO {

    private String roomCategoryId;
    private String roomId;
    private String date;
    private String reason;
    private String message;
}
