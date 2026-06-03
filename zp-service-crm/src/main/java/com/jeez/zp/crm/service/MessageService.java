package com.jeez.zp.crm.service;

import com.jeez.zp.crm.dto.request.MessagePageRequest;
import com.jeez.zp.crm.vo.MessagePageResponseVO;

public interface MessageService {

    MessagePageResponseVO getPage(MessagePageRequest request, Long userId);

    Integer getUnreadCount(Long campId, String groupType, Long userId);

    Boolean markRead(Long campId, Long messageId, Long userId);

    Boolean markAllRead(Long campId, String groupType, Long userId);
}
