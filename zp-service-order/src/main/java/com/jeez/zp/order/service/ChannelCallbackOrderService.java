package com.jeez.zp.order.service;

import com.jeez.zp.order.dto.request.ChannelCallbackOrderCancelRequest;
import com.jeez.zp.order.dto.request.ChannelCallbackOrderDetailRequest;
import com.jeez.zp.order.dto.request.ChannelCallbackOrderModifyRequest;
import com.jeez.zp.order.dto.request.ChannelCallbackOrderPageRequest;
import com.jeez.zp.order.dto.request.ChannelCallbackOrderStatusSyncRequest;
import com.jeez.zp.order.vo.ChannelCallbackOrderOperationResponseVO;
import com.jeez.zp.order.vo.OrderDetailAggregateVO;
import com.jeez.zp.order.vo.OrderPageResponseVO;

public interface ChannelCallbackOrderService {

    OrderPageResponseVO getPage(String channelCode, ChannelCallbackOrderPageRequest request, Long operatorUserId);

    OrderDetailAggregateVO getDetail(String channelCode, ChannelCallbackOrderDetailRequest request, Long operatorUserId);

    ChannelCallbackOrderOperationResponseVO cancel(
            String channelCode,
            ChannelCallbackOrderCancelRequest request,
            Long operatorUserId
    );

    ChannelCallbackOrderOperationResponseVO modify(
            String channelCode,
            ChannelCallbackOrderModifyRequest request,
            Long operatorUserId
    );

    ChannelCallbackOrderOperationResponseVO syncStatus(
            String channelCode,
            ChannelCallbackOrderStatusSyncRequest request,
            Long operatorUserId
    );
}
