package com.jeez.zp.platform.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class WeiRoomCategoryProductGetVO {

    @JsonIgnore
    private Long goodsIdValue;

    private String roomCategoryProductId;
    private String roomCategoryProductName;
    private Integer saleType;
    private Integer stockType;
    private Long sellingPrice;
    private Long originalPrice;
    private Long settlementPrice;
    private Long reducePrice;
    private Integer isCanBooking;
    private Integer stock;
    private Integer stockMode;
    private String photoMediaId;
    private String photoMediaUrl;
    private Integer cancelPolicy;
    private Integer breakfastCount;
    private Integer lunchCount;
    private Integer dinnerCount;
    private Object curDayBookingTime;
    private Object earliestCheckInTime;
    private Object latestCheckInTime;
    private Object latestCheckOutTime;
    private Object hourCheckInTime;
    private Object hourCheckOutTime;
    private Object hourSerialCheckTime;
    private Object isHourLimit;
    private Integer seq;
    private Object expandQuotaExpireTime;
    private List<Object> bindRoomCategoryProductViews;
}
