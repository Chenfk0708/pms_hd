package com.jeez.zp.order.service;

import com.jeez.zp.order.vo.OrderDetailAggregateVO;
import com.jeez.zp.order.vo.OrderPageResponseVO;
import com.jeez.zp.order.vo.StrongReminderPageResponseVO;
import com.jeez.zp.order.vo.WorkspaceOrdersResponseVO;

public interface OrderQueryService {

    OrderPageResponseVO getHousePage(
            Long campId,
            Long userId,
            Integer pageNum,
            Integer pageSize,
            String orderType,
            String searchContent,
            String keyword
    );

    OrderPageResponseVO getLongRentalPage(
            Long campId,
            Long userId,
            Integer pageNum,
            Integer pageSize,
            String orderType,
            String searchContent,
            String keyword,
            String searchCode,
            String orderStatus,
            Long channelId,
            Long roomCategoryId,
            String liveStatus,
            Long poiId
    );

    WorkspaceOrdersResponseVO getWorkspaceOrders(
            Long campId,
            Long userId,
            String orderType,
            Integer page,
            Integer pageNum,
            Integer current,
            Integer pageSize,
            String keyword
    );

    OrderDetailAggregateVO getOrderDetail(Long campId, Long userId, Long orderId);

    StrongReminderPageResponseVO getStrongReminderPage(
            Long campId,
            Long userId,
            Integer page,
            Integer pageNum,
            Integer current,
            Integer pageSize,
            String keyword
    );
}
