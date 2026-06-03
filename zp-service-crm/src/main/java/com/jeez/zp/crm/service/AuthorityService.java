package com.jeez.zp.crm.service;

import com.jeez.zp.crm.dto.request.AuthorityExcludeRequest;
import com.jeez.zp.crm.vo.NotificationAuthorityResponseVO;

public interface AuthorityService {

    NotificationAuthorityResponseVO getNotifications(Long campId, Long userId);

    Boolean exclude(AuthorityExcludeRequest request, Long userId);

    Boolean restore(AuthorityExcludeRequest request, Long userId);
}
