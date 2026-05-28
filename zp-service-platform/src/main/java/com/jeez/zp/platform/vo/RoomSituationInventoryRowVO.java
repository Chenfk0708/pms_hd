package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class RoomSituationInventoryRowVO {

    private String roomId;
    private String roomName;
    private String roomCategoryId;
    private String roomCategoryName;
    private Integer roomCategorySeq;
    private Integer roomSeq;
    private String lockStatus;
    private String cleanStatus;
}
