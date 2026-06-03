package com.jeez.zp.order.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CouponRowVO {

    private String id;
    private String couponId;
    private String name;
    private String couponName;
    private String type;
    private String typeName;
    private Long thresholdAmount;
    private Long discountAmount;
    private String discountText;
    private String scopeText;
    private Integer sendLimit;
    private String sendLimitText;
    private Integer perUserLimit;
    private String perUserLimitText;
    private String sendTimeText;
    private String validityTypeText;
    private String effectiveTimeText;
    private String receiveRuleText;
    private Integer shelfStatus;
    private Integer status;
    private String shelfStatusText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
