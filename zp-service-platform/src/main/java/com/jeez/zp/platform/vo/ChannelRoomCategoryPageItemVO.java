package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class ChannelRoomCategoryPageItemVO {

    private String channelRoomCategoryId;
    private String channelRoomCategoryName;
    private String categoryId;
    private String categoryName;
    private Integer roomCategoryType;
    private Integer goodsType;
    private List<String> channelIds;
    private List<String> channelNames;
    private String totalStock;
    private Integer soldCount;
    private Long lowestSellingPrice;
    private Long lowestOriginalPrice;
    private Integer isCanBooking;
    private String isAvailability;
    private String shelfStatus;
    private String createdAt;
    private String updatedAt;
    private String description;
    private String refundRule;
    private List<ChannelRoomCategoryProductVO> products;
}
