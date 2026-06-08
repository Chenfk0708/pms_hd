package com.jeez.zp.finance.service.impl;

import com.jeez.zp.finance.exception.BusinessException;
import com.jeez.zp.finance.service.CampAccessService;
import com.jeez.zp.order.mapper.UserCampMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CampAccessServiceImpl implements CampAccessService {

    private final UserCampMapper userCampMapper;

    @Override
    public Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        Long currentCampId = userCampMapper.selectCurrentCampId(userId);
        if (currentCampId == null) {
            throw new BusinessException(40401, "\u672A\u627E\u5230\u5F53\u524D\u7528\u6237\u95E8\u5E97");
        }
        if (requestedCampId == null) {
            return currentCampId;
        }
        if (!requestedCampId.equals(currentCampId)) {
            throw new BusinessException(40301, "\u65E0\u6743\u8BBF\u95EE\u5F53\u524D\u95E8\u5E97\u6570\u636E");
        }
        return requestedCampId;
    }
}
