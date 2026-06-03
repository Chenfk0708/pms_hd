package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class ChannelRoomCategoryPageRowVO {

    private Long goodsId;
    private String rawGoodsType;
    private String channelRoomCategoryName;
    private Long categoryId;
    private String categoryName;
    private Integer roomCategoryType;
    private String stockMode;
    private String rawShelfStatus;
    private Integer totalStock;
    private Long soldCount;
    private Long lowestSellingPrice;
    private Long lowestOriginalPrice;
    private String channelIdsCsv;
    private String channelNamesCsv;
    private String createdAt;
    private String updatedAt;
    private String description;
    private String refundRule;
    private Integer sortNo;
}
