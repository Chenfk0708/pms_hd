package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class EditionReplaceOrderRowVO {

    private String replaceOrderId;
    private String orderId;
    private String orderNo;
    private String channelOrderNo;
    private String replaceMonth;
    private String channelName;
    private String roomCategoryName;
    private String roomName;
    private String contactName;
    private String contactMobile;
    private String stayStatus;
    private String stayStatusName;
    private String settlementStatus;
    private String settlementStatusName;
    private String checkInDate;
    private String checkOutDate;
    private String settlementDate;
    private Long settlementAmount;
    private Long replaceAmount;
    private String remark;
}
