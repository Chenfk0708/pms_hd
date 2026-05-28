package com.jeez.zp.platform.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProfitReportRowVO {

    private String date;
    private Integer isTotal;
    private BigDecimal roomFeeMinusCommission;
    private BigDecimal ticketPrice;
    private BigDecimal cateringPrice;
    private BigDecimal otherOrderExpense;
    private BigDecimal writeDownIncome;
    private BigDecimal totalIncome;
    private BigDecimal writeDownExpenses;
    private BigDecimal cleanCost;
    private BigDecimal profitPrice;
    private String profitRate;
}
