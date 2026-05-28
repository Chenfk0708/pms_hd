package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.OrderLedgerDashboardRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.OrderLedgerService;
import com.jeez.zp.platform.vo.OrderLedgerDashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderLedgerController {

    private final OrderLedgerService orderLedgerService;

    @PostMapping("/orderLedger/dashboard/get")
    public HudsonResponse<OrderLedgerDashboardVO> getDashboard(@RequestBody OrderLedgerDashboardRequest request) {
        return HudsonResponse.success(
                orderLedgerService.getDashboard(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("order-ledger-dashboard-get")
        );
    }
}
