package com.jeez.zp.order.controller;

import com.jeez.zp.order.api.HudsonResponse;
import com.jeez.zp.order.api.TraceIdFactory;
import com.jeez.zp.order.dto.request.OrderActionRequest;
import com.jeez.zp.order.dto.request.OrderCreateRequest;
import com.jeez.zp.order.dto.request.OrderGuestsSaveRequest;
import com.jeez.zp.order.security.LoginUserContext;
import com.jeez.zp.order.service.OrderActionService;
import com.jeez.zp.order.vo.OrderActionResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderActionController {

    private final OrderActionService orderActionService;

    @PostMapping("/orders/create")
    public HudsonResponse<OrderActionResponseVO> createOrder(@RequestBody OrderCreateRequest request) {
        return HudsonResponse.success(
                orderActionService.createOrder(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("orders-create")
        );
    }

    @PostMapping("/orders/{id}/cancel")
    public HudsonResponse<OrderActionResponseVO> cancelOrder(@PathVariable("id") Long orderId, @RequestBody OrderActionRequest request) {
        return HudsonResponse.success(
                orderActionService.cancelOrder(parseLong(request.getCampId()), orderId, LoginUserContext.requiredUserId(), request.getReason()),
                TraceIdFactory.next("orders-cancel")
        );
    }

    @PostMapping("/orders/{id}/check-in")
    public HudsonResponse<OrderActionResponseVO> checkIn(@PathVariable("id") Long orderId, @RequestBody OrderActionRequest request) {
        return HudsonResponse.success(
                orderActionService.checkIn(parseLong(request.getCampId()), orderId, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("orders-check-in")
        );
    }

    @PostMapping("/orders/{id}/check-out")
    public HudsonResponse<OrderActionResponseVO> checkOut(@PathVariable("id") Long orderId, @RequestBody OrderActionRequest request) {
        return HudsonResponse.success(
                orderActionService.checkOut(parseLong(request.getCampId()), orderId, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("orders-check-out")
        );
    }

    @PostMapping("/orders/{id}/guests/save")
    public HudsonResponse<OrderActionResponseVO> saveGuests(@PathVariable("id") Long orderId, @RequestBody OrderGuestsSaveRequest request) {
        return HudsonResponse.success(
                orderActionService.saveGuests(parseLong(request.getCampId()), orderId, LoginUserContext.requiredUserId(), request),
                TraceIdFactory.next("orders-guests-save")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
