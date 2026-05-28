package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.SystemConfigItemVO;
import com.jeez.zp.platform.vo.SystemConfigsResponseVO;

public interface SystemConfigService {

    SystemConfigItemVO getCheckInGuideShowStrategy(Long campId, Long userId);

    SystemConfigsResponseVO updateOrderAutoPendingStrategy(Long campId, Long userId, String configKey, String configValue);

    SystemConfigsResponseVO updateOrderAutoSettleStrategy(Long campId, Long userId, String configKey, String configValue);

    SystemConfigsResponseVO updateNegotiateRefundAutomaticAcceptStrategy(Long campId, Long userId, String configKey, String configValue);
}
