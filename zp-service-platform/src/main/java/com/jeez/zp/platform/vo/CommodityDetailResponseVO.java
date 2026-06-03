package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class CommodityDetailResponseVO {

    private String commodityId;
    private String commodityName;
    private String description;
    private String mainPhoto;
    private Long sellingPriceCent;
    private Long originalPriceCent;
    private Long settlementPriceCent;
    private String purchaseTermLabel;
    private List<String> roomCategoryIds;
}
