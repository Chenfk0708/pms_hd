package com.jeez.zp.order.controller;

import com.jeez.zp.order.api.HudsonResponse;
import com.jeez.zp.order.api.TraceIdFactory;
import com.jeez.zp.order.dto.request.OrderDetailRequest;
import com.jeez.zp.order.dto.request.OrderPageRequest;
import com.jeez.zp.order.dto.request.StrongReminderPageRequest;
import com.jeez.zp.order.dto.request.WorkspaceOrdersRequest;
import com.jeez.zp.order.security.LoginUserContext;
import com.jeez.zp.order.service.OrderQueryService;
import com.jeez.zp.order.vo.OrderDetailAggregateVO;
import com.jeez.zp.order.vo.OrderPageResponseVO;
import com.jeez.zp.order.vo.StrongReminderPageResponseVO;
import com.jeez.zp.order.vo.WorkspaceOrdersResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderQueryController {

    private final OrderQueryService orderQueryService;

    @PostMapping({"/orders/house/page/get", "/orders/page/get"})
    public HudsonResponse<OrderPageResponseVO> getHousePage(@RequestBody OrderPageRequest request) {
        if (Integer.valueOf(1).equals(request.getIsLt())) {
            return getLongRentalPage(request);
        }
        return HudsonResponse.success(
                orderQueryService.getHousePage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        firstNonNull(request.getPageNum(), request.getCurrent()),
                        request.getPageSize(),
                        request.getOrderType(),
                        request.getSearchContent(),
                        request.getKeyword()
                ),
                TraceIdFactory.next("orders-house-page-get")
        );
    }

    @PostMapping("/orders/houseLongRental/page/get")
    public HudsonResponse<OrderPageResponseVO> getLongRentalPage(@RequestBody OrderPageRequest request) {
        return HudsonResponse.success(
                orderQueryService.getLongRentalPage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        firstNonNull(request.getPageNum(), request.getCurrent()),
                        request.getPageSize(),
                        request.getOrderType(),
                        request.getSearchContent(),
                        request.getKeyword(),
                        request.getSearchCode(),
                        request.getOrderStatus(),
                        parseLong(request.getChannelId()),
                        parseLong(request.getRoomCategoryId()),
                        request.getLiveStatus(),
                        parseLong(request.getPoiId())
                ),
                TraceIdFactory.next("orders-house-long-rental-page-get")
        );
    }

    @PostMapping("/orders/get")
    public HudsonResponse<WorkspaceOrdersResponseVO> getOrders(@RequestBody WorkspaceOrdersRequest request) {
        return HudsonResponse.success(
                orderQueryService.getWorkspaceOrders(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getOrderType(),
                        request.getPage(),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize(),
                        request.getKeyword()
                ),
                TraceIdFactory.next("orders-get")
        );
    }

    @PostMapping("/orders/detail/get")
    public HudsonResponse<OrderDetailAggregateVO> getOrderDetail(@RequestBody OrderDetailRequest request) {
        return HudsonResponse.success(
                orderQueryService.getOrderDetail(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLong(request.getOrderId())
                ),
                TraceIdFactory.next("orders-detail-get")
        );
    }

    @PostMapping("/orders/strongReminder/page/get")
    public HudsonResponse<StrongReminderPageResponseVO> getStrongReminderPage(@RequestBody StrongReminderPageRequest request) {
        return HudsonResponse.success(
                orderQueryService.getStrongReminderPage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getPage(),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize(),
                        request.getKeyword()
                ),
                TraceIdFactory.next("orders-strong-reminder-page-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }

    private Integer firstNonNull(Integer first, Integer second) {
        return first != null ? first : second;
    }
}
