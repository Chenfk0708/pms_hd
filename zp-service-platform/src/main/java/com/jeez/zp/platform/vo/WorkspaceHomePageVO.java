package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class WorkspaceHomePageVO {

    private Integer nowPredictCheckIn;
    private Integer nowAlreadyCheckIn;
    private Integer nowPredictCheckOut;
    private Integer nowOnSaleNum;
    private Integer userBusyRepairNum;
    private Integer dirtyNum;
    private Integer exceptionOrderNum;
    private Long nowIncome;
}
