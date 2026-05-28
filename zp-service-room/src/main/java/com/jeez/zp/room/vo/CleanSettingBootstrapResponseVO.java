package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class CleanSettingBootstrapResponseVO {

    private List<CleanSettingOptionVO> stores;
    private List<CleanSettingOptionVO> projects;
    private List<CleanSettingOptionVO> statusOptions;
    private List<CleanSettingMetricVO> metrics;
    private List<CleanSettingPolicyRuleVO> policyRules;
    private List<CleanSettingPriceRuleVO> priceRules;
    private List<CleanSettingReminderVO> reminders;
    private List<CleanSettingScheduleVO> schedule;
    private CleanSettingPaginationVO pagination;
    private String requestedAt;
}
