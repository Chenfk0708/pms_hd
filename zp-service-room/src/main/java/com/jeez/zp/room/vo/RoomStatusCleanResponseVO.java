package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class RoomStatusCleanResponseVO {

    private String roomCategoryId;
    private String roomId;
    private String cleanStatus;
    private Integer isDirty;
    private String message;
}
