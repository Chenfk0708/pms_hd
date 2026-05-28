package com.jeez.zp.order.controller;

import com.jeez.zp.order.api.HudsonResponse;
import com.jeez.zp.order.api.TraceIdFactory;
import com.jeez.zp.order.dto.request.OrderReportRequest;
import com.jeez.zp.order.security.LoginUserContext;
import com.jeez.zp.order.service.OrderReportService;
import com.jeez.zp.order.vo.OrderReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderReportController {

    private final OrderReportService orderReportService;

    @PostMapping("/order/report/get")
    public HudsonResponse<OrderReportVO> getReport(@RequestBody OrderReportRequest request) {
        return HudsonResponse.success(
                orderReportService.getReport(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("order-report-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
