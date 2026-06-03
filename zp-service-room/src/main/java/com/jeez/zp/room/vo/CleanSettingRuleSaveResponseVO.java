package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class CleanSettingRuleSaveResponseVO {

    private CleanSettingPolicyRuleVO rule;
    private Integer total;
    private String message;
}
