package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.CleanTaskStatisticsResponseVO;
import com.jeez.zp.platform.vo.CleanerListItemVO;

import java.util.List;

public interface CleanStatisticsService {

    CleanTaskStatisticsResponseVO getStatistics(
            Long campId,
            Long userId,
            Long storeId,
            List<String> roomIds,
            List<String> cleanerIds,
            Long cleanStartTime,
            Long cleanEndTime,
            Integer pageNum,
            Integer pageSize
    );

    List<CleanerListItemVO> getCleaners(Long campId, Long userId);
}
