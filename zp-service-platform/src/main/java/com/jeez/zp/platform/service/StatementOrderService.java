package com.jeez.zp.platform.service;

import java.util.List;

public interface StatementOrderService {

    Object getStatement(
            Long campId,
            Long userId,
            List<Long> poiIds,
            String bookingStartDate,
            String bookingEndDate,
            Boolean breakTemp,
            Integer pageNum,
            Integer current,
            Integer pageSize,
            String exportExcelMenuId
    );
}
