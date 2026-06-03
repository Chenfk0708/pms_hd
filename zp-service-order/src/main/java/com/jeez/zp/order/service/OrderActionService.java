package com.jeez.zp.order.service;

import com.jeez.zp.order.dto.request.OrderCreateRequest;
import com.jeez.zp.order.dto.request.OrderGuestsSaveRequest;
import com.jeez.zp.order.vo.OrderActionResponseVO;

public interface OrderActionService {

    OrderActionResponseVO createOrder(OrderCreateRequest request, Long userId);

    OrderActionResponseVO cancelOrder(Long campId, Long orderId, Long userId, String reason);

    OrderActionResponseVO checkIn(Long campId, Long orderId, Long userId);

    OrderActionResponseVO checkOut(Long campId, Long orderId, Long userId);

    OrderActionResponseVO saveGuests(Long campId, Long orderId, Long userId, OrderGuestsSaveRequest request);
}
