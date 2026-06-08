package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class RoomCategorySaleStatusSaveResponseVO {

    private String roomCategoryId;
    private String date;
    private Boolean saleEnabled;
}
