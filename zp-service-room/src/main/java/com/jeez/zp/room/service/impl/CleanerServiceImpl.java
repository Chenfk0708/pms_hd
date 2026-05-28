package com.jeez.zp.room.service.impl;

import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.CleanTaskMapper;
import com.jeez.zp.room.mapper.UserCampMapper;
import com.jeez.zp.room.service.CleanerService;
import com.jeez.zp.room.vo.CleanTaskOptionVO;
import com.jeez.zp.room.vo.CleanerListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CleanerServiceImpl implements CleanerService {

    private final CleanTaskMapper cleanTaskMapper;
    private final UserCampMapper userCampMapper;

    @Override
    public List<CleanerListItemVO> getCleaners(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        return cleanTaskMapper.selectCleaners(resolvedCampId).stream()
                .map(this::toCleaner)
                .toList();
    }

    private CleanerListItemVO toCleaner(CleanTaskOptionVO option) {
        CleanerListItemVO cleaner = new CleanerListItemVO();
        cleaner.setCleanerId(option.getId());
        cleaner.setCleanerName(option.getLabel());
        return cleaner;
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        Long currentCampId = userCampMapper.selectCurrentCampId(userId);
        if (currentCampId == null) {
            throw new BusinessException(40401, "\u672A\u627E\u5230\u5F53\u524D\u7528\u6237\u95E8\u5E97");
        }
        if (requestedCampId == null) {
            return currentCampId;
        }
        if (!requestedCampId.equals(currentCampId)) {
            throw new BusinessException(40301, "\u65E0\u6743\u8BBF\u95EE\u5F53\u524D\u95E8\u5E97\u4FDD\u6D01\u7EDF\u8BA1");
        }
        return requestedCampId;
    }
}
