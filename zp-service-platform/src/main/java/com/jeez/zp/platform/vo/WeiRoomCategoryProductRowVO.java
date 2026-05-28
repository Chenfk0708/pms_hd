package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class WeiRoomCategoryProductRowVO {

    private Long goodsId;
    private String roomCategoryProductId;
    private String roomCategoryProductName;
    private Long sellingPrice;
    private Long originalPrice;
    private Long settlementPrice;
    private Integer isCanBooking;
    private Integer stock;
    private Integer stockMode;
    private String photoMediaId;
    private String photoMediaUrl;
    private Integer seq;
}
