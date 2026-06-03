package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class CommodityDetailRowVO {

    private Long goodsId;
    private String commodityName;
    private String description;
    private String mainPhoto;
    private Long sellingPriceCent;
    private Long originalPriceCent;
    private Long settlementPriceCent;
    private String purchaseTermLabel;
}
