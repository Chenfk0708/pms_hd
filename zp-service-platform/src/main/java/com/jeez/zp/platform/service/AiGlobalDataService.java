package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.AiGlobalReminderPageResponseVO;
import com.jeez.zp.platform.vo.AiGlobalExportResponseVO;
import com.jeez.zp.platform.vo.AiGlobalReminderActionResponseVO;
import com.jeez.zp.platform.vo.AiGlobalShopStatusVO;

import java.util.List;

public interface AiGlobalDataService {

    AiGlobalReminderPageResponseVO getStrongReminderPage(
            Long campId,
            Long userId,
            Integer page,
            Integer pageNum,
            Integer current,
            Integer pageSize
    );

    List<AiGlobalShopStatusVO> getShopStatuses(Long campId, Long userId, Integer status);

    AiGlobalExportResponseVO createExportTask(Long campId, Long userId, String channel, String attention, String roomKeyword);

    AiGlobalReminderActionResponseVO postponeStrongReminder(Long campId, Long userId, String reminderId, String orderNo);

    AiGlobalReminderActionResponseVO resolveStrongReminder(Long campId, Long userId, String reminderId, String orderNo);
}
