package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryPricingTableResponseVO {

    private List<RoomCategoryPricingHeadCellVO> head;
    private List<RoomCategoryPricingBodyRowVO> body;
}
