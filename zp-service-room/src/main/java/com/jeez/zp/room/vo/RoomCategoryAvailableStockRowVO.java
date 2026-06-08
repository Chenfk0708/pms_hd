package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class RoomCategoryAvailableStockRowVO {

    private String roomCategoryId;
    private String date;
    private Long totalStock;
}
