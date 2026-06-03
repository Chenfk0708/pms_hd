package com.jeez.zp.platform.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ChannelRoomCategoryProductVO {

    @JsonIgnore
    private Long goodsIdValue;

    private String roomCategoryProductId;
    private String roomCategoryProductName;
    private Long sellingPrice;
    private Long originalPrice;
    private Integer stock;
}
