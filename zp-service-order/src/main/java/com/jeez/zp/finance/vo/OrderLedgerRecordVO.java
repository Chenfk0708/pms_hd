package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class OrderLedgerRecordVO {
    private String id;
    private String poiId;
    private String typeLabel;
    private String sourceLabel;
    private String orderId;
    private String projectLabel;
    private Double amount;
    private Double debtAmount;
    private String paymentWayLabel;
    private String paymentNo;
    private String paymentTime;
    private String createdAt;
    private String roomLabel;
    private String remark;
    private String operatorName;
}
