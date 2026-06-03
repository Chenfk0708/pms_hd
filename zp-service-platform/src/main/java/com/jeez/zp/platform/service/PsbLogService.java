package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.PsbLogPageDataVO;
import com.jeez.zp.platform.vo.PsbLogRowVO;

public interface PsbLogService {

    PsbLogPageDataVO getPage(
            Long campId,
            Long userId,
            Long poiId,
            String keyword,
            String bizType,
            String state,
            Integer pageNum,
            Integer pageSize
    );

    PsbLogRowVO retry(Long campId, Long userId, Long guestId, String orderNo);
}
