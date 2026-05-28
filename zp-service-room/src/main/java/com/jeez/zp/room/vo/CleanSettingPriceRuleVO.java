package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class CleanSettingPriceRuleVO {

    private String id;
    private String name;
    private String projectId;
    private String cleanType;
    private String amount;
    private String settlementMode;
    private String status;
}
