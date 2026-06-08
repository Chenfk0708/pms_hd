package com.jeez.zp.room.dto.request;

import lombok.Data;

@Data
public class RoomCategorySaleStatusSaveRequest {

    private String campId;
    private String roomCategoryId;
    private String date;
    private Boolean saleEnabled;
}
