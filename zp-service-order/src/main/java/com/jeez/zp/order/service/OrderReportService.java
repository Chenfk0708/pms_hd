package com.jeez.zp.order.service;

import com.jeez.zp.order.vo.OrderReportVO;

public interface OrderReportService {

    OrderReportVO getReport(Long campId, Long userId);
}
