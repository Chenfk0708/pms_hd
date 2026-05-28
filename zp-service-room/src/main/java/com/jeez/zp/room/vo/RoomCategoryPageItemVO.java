package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryPageItemVO {

    private String roomCategoryId;
    private String campId;
    private String saleCampId;
    private String upstreamCampId;
    private String downstreamCampId;
    private Integer fromType;
    private String channelId;
    private String parentId;
    private String mainPhotoMediaId;
    private String mainPhotoMediaUrl;
    private String mainPhoto;
    private Integer inventory;
    private Integer checkinGuestCount;
    private String roomCategoryTransferId;
    private String checkinGuideId;
    private Integer isCampHasMultiInventoryRoomCategory;
    private Integer isTransfer;
    private Integer priority;
    private Long basePrice;
    private Long weekendPrice;
    private Long holidayPrice;
    private Integer channelOrderTotalNum;
    private Integer expectedChannelOrderTotalNum;
    private List<RoomCategoryRoomViewVO> roomViews;
    private Integer isAvailability;
    private Integer auditStatus;
    private String failReason;
    private Long depositPrice;
    private String fullAddress;
    private Integer waitMappingChannelNum;
    private PoiViewVO poiView;
    private List<Object> icsInfoViews;
    private List<RoomCategoryProductInfoVO> roomCategoryProductInfoViews;
    private List<Object> roomCategorySystemConfigInfos;
    private Integer isSupportHotelProduct;
    private Integer isCanDelete;
    private String deleteMessage;
    private String name;
    private List<Object> linkRcs;
    private List<Object> byLinkRcs;
    private List<Object> channels;
    private String id;
    private String roomCategoryName;
    private String poiId;
    private String poiName;
    private Integer roomNum;
    private String roomNames;
    private String roomCategoryGroupId;
    private String roomCategoryGroupName;
    private String displayName;
    private Integer earliestCheckInTime;
    private Integer latestCheckInTime;
    private Integer latestCheckOutTime;
    private String highlightDescription;
    private String nearbyDescription;
    private String articleDescription;
}
