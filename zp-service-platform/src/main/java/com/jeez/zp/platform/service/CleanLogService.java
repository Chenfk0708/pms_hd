package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.CleanLogPageDataVO;

import java.util.List;

public interface CleanLogService {

    CleanLogPageDataVO getPage(
            Long campId,
            Long userId,
            Long poiId,
            List<String> roomIds,
            Long operatorId,
            Long operatorStartTime,
            Long operatorEndTime,
            Integer pageNum,
            Integer pageSize
    );
}
