package com.jeez.zp.platform.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class IncomeReportRowVO {

    private String key;
    private String label;
    private Integer isTotal;
    private BigDecimal roomFeeMinusCommission;
    private BigDecimal channelCommission;
    private BigDecimal roomFeeIncludingCommission;
    private BigDecimal allDayRoomFeeIncludingCommission;
    private BigDecimal hourRoomFeeIncludingCommission;
    private BigDecimal otherExpense;
    private BigDecimal accommodationExpense;
    private BigDecimal cateringExpense;
    private BigDecimal supermarketExpense;
    private BigDecimal entertainmentExpense;
    private BigDecimal venueExpense;
    private BigDecimal orderTotalIncome;
    private BigDecimal manualIncome;
    private BigDecimal manualAccommodationIncome;
    private BigDecimal manualCateringIncome;
    private BigDecimal manualSupermarketIncome;
    private BigDecimal manualEntertainmentIncome;
    private BigDecimal manualVenueIncome;
    private BigDecimal businessIncomeIncludingCommission;
    private BigDecimal businessIncomeMinusCommission;
    private String roomFeeMinusCommissionRatio;
    private String channelCommissionRatio;
    private String detailContext;
}
