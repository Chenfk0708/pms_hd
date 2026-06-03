package com.jeez.zp.room.service;

import com.jeez.zp.room.dto.request.CleanTaskCreateRequest;
import com.jeez.zp.room.vo.CleanTaskActionResponseVO;
import com.jeez.zp.room.vo.CleanTaskDashboardResponseVO;
import com.jeez.zp.room.vo.CleanTaskExportResponseVO;
import com.jeez.zp.room.vo.CleanTaskStatisticsResponseVO;
import com.jeez.zp.room.vo.CleanStatisticsDashboardResponseVO;
import com.jeez.zp.room.vo.CleanStatisticsExportResponseVO;

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

    CleanTaskActionResponseVO create(CleanTaskCreateRequest request, Long userId);

    CleanTaskActionResponseVO notify(Long campId, Long userId, List<String> taskIds);

    CleanTaskExportResponseVO export(
            Long campId,
            Long userId,
            Long poiId,
            String cleanTime,
            Long roomId,
            String cleanType,
            String cleanStatus,
            List<String> cleanerIds
    );

    CleanTaskStatisticsResponseVO getStatistics(
            Long campId,
            Long userId,
            Long poiId,
            List<String> roomIds,
            List<String> cleanerIds,
            Long cleanStartTime,
            Long cleanEndTime,
            Integer pageNum,
            Integer pageSize
    );

    CleanStatisticsDashboardResponseVO getStatisticsDashboard(
            Long campId,
            Long userId,
            Long poiId,
            List<String> roomIds,
            List<String> cleanerIds,
            Long cleanStartTime,
            Long cleanEndTime,
            Integer pageNum,
            Integer pageSize
    );

    CleanStatisticsExportResponseVO exportStatistics(
            Long campId,
            Long userId,
            Long poiId,
            List<String> roomIds,
            List<String> cleanerIds,
            Long cleanStartTime,
            Long cleanEndTime
    );
}
