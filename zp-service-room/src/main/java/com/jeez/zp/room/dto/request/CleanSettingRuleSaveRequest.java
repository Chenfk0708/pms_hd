package com.jeez.zp.room.dto.request;

import com.jeez.zp.room.vo.CleanSettingPolicyRuleVO;
import lombok.Data;

@Data
public class CleanSettingRuleSaveRequest {

    private String campId;
    private CleanSettingPolicyRuleVO rule;
}
