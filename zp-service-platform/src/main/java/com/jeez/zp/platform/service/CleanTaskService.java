package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.CleanTaskDashboardResponseVO;

import java.util.List;

public interface CleanTaskService {

    CleanTaskDashboardResponseVO getPage(
            Long campId,
            Long userId,
            Long poiId,
            String cleanTime,
            Long roomId,
            String cleanType,
            String cleanStatus,
            List<String> cleanerIds,
            Integer pageNum,
            Integer pageSize
    );
}
