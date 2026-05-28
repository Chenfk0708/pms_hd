package com.jeez.zp.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderLedgerQueryRowVO {

    private String ledgerEntryId;
    private String poiId;
    private String entryType;
    private String paymentTypeId;
    private String paymentTypeName;
    private String paymentWayId;
    private String paymentWayName;
    private String orderId;
    private String sourceType;
    private Long amountCent;
    private LocalDateTime occurredAt;
    private String operatorName;
    private String remark;
    private String orderNo;
    private String outOrderNo;
    private String orderStatus;
    private String guestName;
    private String guestMobile;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String roomCategoryName;
    private String roomName;
}
