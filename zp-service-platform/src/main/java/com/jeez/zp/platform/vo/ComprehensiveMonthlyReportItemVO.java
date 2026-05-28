package com.jeez.zp.platform.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ComprehensiveMonthlyReportItemVO {

    private String date;
    private Long startDate;
    private Long endDate;
    private BigDecimal includeCommissionRoomPrice;
    private BigDecimal orderOtherExpense;
    private BigDecimal writeDownIncome;
    private BigDecimal businessIncome;
    private BigDecimal occ;
    private BigDecimal adr;
    private BigDecimal revPar;
    private Long createTime;
    private String userId;
    private String userName;
    private Integer inventory;
    private Integer openRoomCount;
}
