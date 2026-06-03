package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.SystemConfigItemVO;
import com.jeez.zp.platform.vo.SystemConfigsResponseVO;
import com.jeez.zp.platform.vo.CommonsResponseVO;
import com.jeez.zp.platform.vo.UserShortcutResponseVO;

import java.util.List;

public interface SystemConfigService {

    SystemConfigItemVO getCheckInGuideShowStrategy(Long campId, Long userId);

    SystemConfigItemVO getCheckWifiShowStrategy(Long campId, Long userId);

    SystemConfigsResponseVO updateCheckInGuideShowStrategy(Long campId, Long userId, Integer isCheckInGuideIdentityRegCompleted, Integer isCheckInGuideVerifyPayDeposit);

    SystemConfigsResponseVO updateCheckWifiShowStrategy(Long campId, Long userId, Integer isWifiDisplayEnabled);

    SystemConfigsResponseVO updateOrderAutoPendingStrategy(Long campId, Long userId, String configKey, String configValue);

    SystemConfigsResponseVO updateOrderAutoSettleStrategy(Long campId, Long userId, String configKey, String configValue);

    SystemConfigsResponseVO updateNegotiateRefundAutomaticAcceptStrategy(Long campId, Long userId, String configKey, String configValue);

    SystemConfigsResponseVO updateNightAudit(Long campId, Long userId, Integer isNightAudit, Integer autoNightAuditTime);

    SystemConfigsResponseVO updateFinanceStrategy(Long campId, Long userId, Integer orderAmortizeStrategy);

    SystemConfigsResponseVO updateVendibleTypes(Long campId, Long userId, List<Integer> vendibleTypes);

    CommonsResponseVO getCommons(Long campId, Long userId, String code);

    UserShortcutResponseVO getUserShortcuts(Long requestedUserId, Long currentUserId);
}

