package com.jeez.zp.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ComprehensiveMonthlyReportQueryRowVO {

    private String orderId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer dayNum;
    private Long totalPriceCent;
    private Long commissionPriceCent;
    private Long businessIncomeCent;
    private LocalDateTime createdAt;
    private String userId;
    private String userName;
}
