package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryPricingBodyRowVO {

    private String roomCategoryId;
    private String rcpi;
    private String roomCategoryName;
    private String rcn;
    private String channelId;
    private String channelName;
    private String cn;
    private List<RoomCategoryPricingCellVO> cells;
}
