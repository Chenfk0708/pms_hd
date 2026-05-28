package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.ComprehensiveMonthlyReportPageResponseVO;

public interface ComprehensiveMonthlyReportService {

    ComprehensiveMonthlyReportPageResponseVO getPage(
            Long campId,
            Long userId,
            String startDate,
            String endDate,
            Integer page,
            Integer pageNum,
            Integer pageSize,
            Integer current
    );
}
