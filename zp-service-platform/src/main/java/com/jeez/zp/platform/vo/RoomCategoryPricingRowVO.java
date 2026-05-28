package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class RoomCategoryPricingRowVO {

    private String roomCategoryId;
    private String roomCategoryName;
    private String channelId;
    private String channelName;
    private Integer commissionRate;
    private String productName;
    private Long normalPrice;
    private Long normalActualSalePrice;
}
