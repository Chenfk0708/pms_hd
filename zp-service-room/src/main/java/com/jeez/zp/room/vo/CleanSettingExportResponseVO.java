package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class CleanSettingExportResponseVO {

    private String fileName;
    private String contentType;
    private Integer total;
    private List<CleanSettingPolicyRuleVO> policyRules;
    private List<CleanSettingPriceRuleVO> priceRules;
}
