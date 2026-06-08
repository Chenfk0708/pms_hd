package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class LedgerEntryRowVO {
    private String id;
    private String accountName;
    private Integer isIncome;
    private String typeName;
    private Double amount;
    private String paymentWayName;
    private String roomCategoryName;
    private String roomName;
    private String note;
    private String operatorName;
    private String channelName;
    private String gmtCreate;
}
