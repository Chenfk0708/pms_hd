package com.jeez.zp.platform.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderLedgerPaymentRecordVO {

    private String id;
    private String typeLabel;
    private String roomLabel;
    private String projectLabel;
    private String paymentWayLabel;
    private BigDecimal amount;
    private String paymentNo;
    private String paidAt;
    private String remark;
    private String actionLabel;
}
