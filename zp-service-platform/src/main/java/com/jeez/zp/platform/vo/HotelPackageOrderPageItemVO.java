package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class HotelPackageOrderPageItemVO {

    private String orderId;
    private String roomCategoryName;
    private Integer count;
    private Long unitPrice;
    private Long schedulePriceDiff;
    private Long paidAmount;
    private String contactPhone;
    private String orderStateName;
    private String refundDisplayStateName;
    private String orderChannelName;
    private String bookedAt;
}
