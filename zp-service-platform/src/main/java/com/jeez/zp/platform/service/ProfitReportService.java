package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.ProfitReportPageResponseVO;

public interface ProfitReportService {

    ProfitReportPageResponseVO getProfitReport(
            Long campId,
            Long userId,
            String startDate,
            String endDate,
            Long poiId,
            Long roomCategoryId,
            Long roomCategoryGroupId,
            Long channelId,
            Long roomId,
            Integer isCleanCost,
            Integer pageNum,
            Integer current,
            Integer pageSize
    );
}
