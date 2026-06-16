package com.jeez.zp.room.dto.request;

import lombok.Data;

@Data
public class RoomStatusCleanRequest {

    private String campId;
    private String roomCategoryId;
    private String roomId;
    private String cleanStatus;
}
