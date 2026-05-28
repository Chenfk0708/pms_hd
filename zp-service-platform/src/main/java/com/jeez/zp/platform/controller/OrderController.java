package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.OrderPageRequest;
import com.jeez.zp.platform.dto.request.OrderReportRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.OrderQueryService;
import com.jeez.zp.platform.vo.OrderPageResponseVO;
import com.jeez.zp.platform.vo.OrderReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderQueryService orderQueryService;

    @PostMapping("/order/report/get")
    public HudsonResponse<OrderReportVO> getReport(@RequestBody OrderReportRequest request) {
        return HudsonResponse.success(
                orderQueryService.getReport(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("order-report-get")
        );
    }

    @PostMapping("/orders/page/get")
    public HudsonResponse<OrderPageResponseVO> getPage(@RequestBody OrderPageRequest request) {
        return HudsonResponse.success(
                orderQueryService.getPage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getPageNum(),
                        request.getPageSize(),
                        request.getRoomCategoryTypes(),
                        request.getOrderStates(),
                        request.getCategoryIds(),
                        request.getOrderChannelIds(),
                        request.getPaymentWayIds(),
                        request.getRefundDisplayState(),
                        parseLong(request.getBookedStartDate()),
                        parseLong(request.getBookedEndDate()),
                        request.getOrderType(),
                        request.getIsLt(),
                        request.getSearchContent(),
                        request.getKeyword(),
                        request.getSearchCode(),
                        request.getDateType(),
                        request.getOrderStatus(),
                        parseLong(request.getChannelId()),
                        parseLong(request.getRoomCategoryId()),
                        request.getLiveStatus(),
                        parseLong(request.getPoiId())
                ),
                TraceIdFactory.next("orders-page-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
