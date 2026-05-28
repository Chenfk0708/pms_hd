package com.jeez.zp.platform.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderLedgerRecordVO {

    private String id;
    private String poiId;
    private String typeLabel;
    private String sourceLabel;
    private String orderId;
    private String projectLabel;
    private BigDecimal amount;
    private BigDecimal debtAmount;
    private String paymentWayLabel;
    private String paymentNo;
    private String paymentTime;
    private String createdAt;
    private String roomLabel;
    private String remark;
    private String operatorName;
    private OrderLedgerDetailVO detail;
}
