package com.jeez.zp.order.vo;

import lombok.Data;

@Data
public class OrderReportVO {

    private Integer todayNewOrder;
    private Integer todayPredictCheckIn;
    private Integer staying;
    private Integer todayPredictCheckOut;
    private Integer tomorrowCheckIn;
    private Integer tomorrowCheckOut;
    private Integer pending;
    private Integer refunding;
    private Integer exception;
}
