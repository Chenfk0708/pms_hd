package com.jeez.zp.platform.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WorkspaceGrowthTrendItemVO {

    private String date;
    private BigDecimal businessIncome;
    private BigDecimal occ;
    private BigDecimal adr;
    private BigDecimal revPar;
    private Integer openRoomCount;
}
