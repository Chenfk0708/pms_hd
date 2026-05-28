package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class WeiRoomCategoryPageItemVO {

    private String campId;
    private String channelRoomCategoryId;
    private String channelRoomCategoryName;
    private String parentRoomCategoryId;
    private String mainPhotoMediaId;
    private String mainPhotoMediaUrl;
    private String mainPhoto;
    private Integer bedroomNum;
    private Integer ableArea;
    private Integer hallNum;
    private Integer personCapacity;
    private Integer bedNum;
    private List<Object> bedMetas;
    private Long lowestSellingPrice;
    private Long lowestSettlementPrice;
    private Long lowestOriginalPrice;
    private Long reducePrice;
    private Integer isCanBooking;
    private String totalStock;
    private Integer roomCategoryType;
    private Integer goodsType;
    private Integer isLongTermEffective;
    private Long effectiveStartTime;
    private Long effectiveEndTime;
    private Integer poiType;
    private String parentPoiId;
    private Object poiTags;
    private String channelPoiId;
    private Object promotionDirectRatio;
    private String editionId;
    private String editionLevel;
    private String editionUpgradeType;
    private Integer isTransitionEdition;
    private Integer isFreeEdition;
    private String giftType;
    private Object outRuleJson;
    private Object channelOutRuleJson;
    private String applyId;
    private String isAvailability;
    private String description;
    private Object attIds;
    private String contractAgreementUrl;
    private Object bindRoomCategoryViews;
    private Object bindServiceViews;
    private List<WeiRoomCategoryProductGetVO> roomCategoryProductGetViews;
}
