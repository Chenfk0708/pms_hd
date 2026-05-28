package com.jeez.zp.platform.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderLedgerRoomBreakdownVO {

    private String date;
    private String roomLabel;
    private BigDecimal amount;
}
