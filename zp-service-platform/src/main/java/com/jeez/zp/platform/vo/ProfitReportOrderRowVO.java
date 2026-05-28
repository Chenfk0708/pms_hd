package com.jeez.zp.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProfitReportOrderRowVO {

    private String orderId;
    private String orderStatus;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer dayNum;
    private Long businessIncomeCent;
}
