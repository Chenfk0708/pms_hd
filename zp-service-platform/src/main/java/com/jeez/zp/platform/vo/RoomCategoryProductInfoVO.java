package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class RoomCategoryProductInfoVO {

    private String roomCategoryProductId;
    private String roomCategoryProductName;
    private Integer saleType;
    private Integer serialCheckInTime;
    private Integer earliestCheckInTime;
    private Integer latestCheckInTime;
    private Integer latestCheckOutTime;
    private Integer hourCheckInTime;
    private Integer hourCheckOutTime;
    private Integer hourSerialCheckTime;
    private Integer isHourLimit;
    private Integer cancelPolicy;
    private Integer breakfastCount;
    private Integer isPerfectRoomCategoryProduct;
    private Long normalPrice;
    private Long weekendPrice;
    private Long holidayPrice;
    private Integer stockMode;
}
