package com.jeez.zp.room.dto.request;

import lombok.Data;

@Data
public class RoomStatusCloseRequest {

    private String campId;
    private String roomCategoryId;
    private String roomId;
    private String date;
    private String reason;
}
