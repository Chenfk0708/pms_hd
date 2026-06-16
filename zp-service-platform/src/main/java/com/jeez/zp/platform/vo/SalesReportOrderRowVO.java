package com.jeez.zp.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SalesReportOrderRowVO {

    private String orderId;
    private String orderType;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer dayNum;
    private Long totalPriceCent;
    private Long totalPayPriceCent;
    private Long commissionCent;
    private Long settlementAmountCent;
    private String poiName;
    private String channelName;
    private String roomCategoryName;
    private String roomName;
}
