package com.jeez.zp.platform.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderLedgerDetailVO {

    private String channelName;
    private String channelOrderNo;
    private String roomLabel;
    private String statusLabel;
    private BigDecimal totalAmount;
    private String stayRange;
    private String guestSummary;
    private String productName;
    private String breakdownTitle;
    private BigDecimal breakdownAmount;
    private BigDecimal totalIncome;
    private List<OrderLedgerRoomBreakdownVO> roomBreakdown;
    private List<OrderLedgerExtraLineVO> extraLines;
    private List<OrderLedgerPaymentRecordVO> paymentRecords;
}
