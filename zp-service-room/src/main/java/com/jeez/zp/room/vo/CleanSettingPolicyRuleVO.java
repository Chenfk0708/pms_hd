package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class CleanSettingPolicyRuleVO {

    private String id;
    private String name;
    private String storeId;
    private String storeName;
    private String projectId;
    private String roomScope;
    private String trigger;
    private String cleanerGroup;
    private String status;
    private String updatedAt;
    private String detail;
}
