package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.IncomeReportPageResponseVO;

public interface IncomeReportService {

    IncomeReportPageResponseVO getIncomeReport(
            Long campId,
            Long userId,
            String startDate,
            String endDate,
            Long poiId,
            Long roomCategoryId,
            Long roomCategoryGroupId,
            Long channelId,
            Long roomId,
            Integer queryType,
            Integer pageNum,
            Integer current,
            Integer pageSize
    );
}
