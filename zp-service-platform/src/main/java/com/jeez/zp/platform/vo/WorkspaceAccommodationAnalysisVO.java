package com.jeez.zp.platform.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class WorkspaceAccommodationAnalysisVO {

    private BigDecimal businessIncome;
    private BigDecimal roomFeePriceIncludingCommission;
    private BigDecimal writeDownIncome;
    private BigDecimal otherOrderExpense;
    private BigDecimal occ;
    private BigDecimal adr;
    private BigDecimal revPar;
    private Integer openRoomCount;
    private Integer roomCount;
    private Integer allDayOpenRoomCount;
    private Integer hourOpenRoomCount;
    private List<WorkspaceGrowthTrendItemVO> growthTrendAnalysisList;
    private List<WorkspaceOrderOriginItemVO> orderOriginAnalysisList;
}
