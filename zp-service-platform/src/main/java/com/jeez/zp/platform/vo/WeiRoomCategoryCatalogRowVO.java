package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class WeiRoomCategoryCatalogRowVO {

    private Long goodsId;
    private String rawGoodsType;
    private String channelRoomCategoryName;
    private Integer roomCategoryType;
    private String parentRoomCategoryId;
    private String mainPhotoMediaId;
    private String mainPhotoMediaUrl;
    private String mainPhoto;
    private Long defaultSellingPrice;
    private Long defaultOriginalPrice;
    private Long defaultSettlementPrice;
    private String stockMode;
    private String shelfStatus;
    private Long effectiveStartTime;
    private Long effectiveEndTime;
    private String description;
    private String applyId;
    private Integer sortNo;
}
