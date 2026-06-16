package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.SalesReportPageResponseVO;

import java.util.List;

public interface SalesReportService {

    SalesReportPageResponseVO getSalesReport(
            Long campId,
            Long userId,
            String startDate,
            String endDate,
            List<Long> poiIds,
            List<Long> roomCategoryIds,
            List<Long> roomCategoryGroupIds,
            List<Long> channelIds,
            List<Long> roomIds,
            Integer queryType,
            Integer pageNum,
            Integer current,
            Integer pageSize
    );
}
