package com.jeez.zp.room.service;

import com.jeez.zp.room.vo.CleanSettingBootstrapResponseVO;
import com.jeez.zp.room.vo.CleanSettingExportResponseVO;
import com.jeez.zp.room.vo.CleanSettingPolicyRuleVO;
import com.jeez.zp.room.vo.CleanSettingRuleSaveResponseVO;

public interface CleanSettingService {

    CleanSettingBootstrapResponseVO bootstrap(
            Long campId,
            Long userId,
            String businessDate,
            String storeId,
            String projectId,
            String status,
            Integer page,
            Integer pageSize
    );

    CleanSettingRuleSaveResponseVO savePolicyRule(
            Long campId,
            Long userId,
            CleanSettingPolicyRuleVO rule
    );

    CleanSettingExportResponseVO export(
            Long campId,
            Long userId,
            String businessDate,
            String storeId,
            String projectId,
            String status
    );
}
