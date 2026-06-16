package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class RoomCategoryPriceSnapshotRowVO {

    private String roomCategoryId;
    private String channelId;
    private String bizDate;
    private Long priceCent;
    private String status;
}
