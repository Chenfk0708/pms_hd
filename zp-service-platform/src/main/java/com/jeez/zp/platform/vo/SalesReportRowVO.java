package com.jeez.zp.platform.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesReportRowVO {

    private String date;
    private String month;
    private String poiName;
    private String channelName;
    private String roomCategoryName;
    private String roomName;
    private Integer isTotal;
    private Integer roomCount;
    private Integer canSaleRoomCount;
    private Integer openRoomCount;
    private Integer allDayOpenRoomCount;
    private Integer hourOpenRoomCount;
    private String occ;
    private BigDecimal adr;
    private BigDecimal adrAfterCommission;
    private BigDecimal revPar;
    private BigDecimal revParAfterCommission;
    private BigDecimal roomFeeMinusCommission;
    private BigDecimal commission;
    private BigDecimal roomFeeIncludingCommission;
    private Integer orderCount;
    private String saleRoomRate;
    private String allDaySaleRoomRate;
    private String hourSaleRoomRate;
    private String orderRate;
}
