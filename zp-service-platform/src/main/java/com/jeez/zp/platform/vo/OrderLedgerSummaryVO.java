package com.jeez.zp.platform.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderLedgerSummaryVO {

    private BigDecimal netIncome;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
}
