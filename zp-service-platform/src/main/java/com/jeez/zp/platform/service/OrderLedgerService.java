package com.jeez.zp.platform.service;

import com.jeez.zp.platform.dto.request.OrderLedgerDashboardRequest;
import com.jeez.zp.platform.vo.OrderLedgerDashboardVO;

public interface OrderLedgerService {

    OrderLedgerDashboardVO getDashboard(OrderLedgerDashboardRequest request, Long userId);
}
