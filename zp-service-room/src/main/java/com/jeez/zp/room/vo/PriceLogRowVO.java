package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class PriceLogRowVO {

    private String logId;
    private String roomCategoryName;
    private String priceDate;
    private String actionContent;
    private String adjustTypeName;
    private String channelName;
    private Long channelSalePrice;
    private String operatorName;
    private String operationTime;

    private Long snapshotId;
    private Long roomCategoryId;
    private Long channelId;
    private String priceType;
    private Long previousPriceCent;
}
