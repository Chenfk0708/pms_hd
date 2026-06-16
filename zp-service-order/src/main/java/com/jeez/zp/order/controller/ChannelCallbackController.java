package com.jeez.zp.order.controller;

import com.jeez.zp.order.api.HudsonResponse;
import com.jeez.zp.order.api.TraceIdFactory;
import com.jeez.zp.order.dto.request.ChannelCallbackOrderCancelRequest;
import com.jeez.zp.order.dto.request.ChannelCallbackOrderDetailRequest;
import com.jeez.zp.order.dto.request.ChannelCallbackOrderImportRequest;
import com.jeez.zp.order.dto.request.ChannelCallbackOrderModifyRequest;
import com.jeez.zp.order.dto.request.ChannelCallbackOrderPageRequest;
import com.jeez.zp.order.dto.request.ChannelCallbackOrderStatusSyncRequest;
import com.jeez.zp.order.exception.BusinessException;
import com.jeez.zp.order.service.ChannelCallbackOrderService;
import com.jeez.zp.order.service.ChannelOrderImportService;
import com.jeez.zp.order.vo.ChannelCallbackOrderOperationResponseVO;
import com.jeez.zp.order.vo.ChannelOrderImportResponseVO;
import com.jeez.zp.order.vo.OrderDetailAggregateVO;
import com.jeez.zp.order.vo.OrderPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChannelCallbackController {

    private static final String TEST_TOKEN_HEADER = "X-Channel-Test-Token";
    private static final String OPERATOR_ID_HEADER = "X-Channel-Operator-Id";
    private static final String AUTH_FAILED_MESSAGE = "第三方渠道回调认证失败";

    private final ChannelOrderImportService channelOrderImportService;
    private final ChannelCallbackOrderService channelCallbackOrderService;

    @Value("${jeez.channel.callback.test-token:}")
    private String channelCallbackTestToken;

    @PostMapping("/channelCallbacks/{channelCode}/orders/import")
    public HudsonResponse<ChannelOrderImportResponseVO> importOrder(
            @PathVariable String channelCode,
            @RequestHeader(name = TEST_TOKEN_HEADER, required = false) String testToken,
            @RequestHeader(name = OPERATOR_ID_HEADER, required = false) String operatorId,
            @RequestBody ChannelCallbackOrderImportRequest request
    ) {
        validateTestToken(testToken);
        Long operatorUserId = parseOperatorUserId(operatorId);
        return HudsonResponse.success(
                channelOrderImportService.importOrder(request.toImportRequest(channelCode), operatorUserId),
                TraceIdFactory.next("channel-callback-order-import")
        );
    }

    @PostMapping("/channelCallbacks/{channelCode}/orders/page/get")
    public HudsonResponse<OrderPageResponseVO> getOrders(
            @PathVariable String channelCode,
            @RequestHeader(name = TEST_TOKEN_HEADER, required = false) String testToken,
            @RequestHeader(name = OPERATOR_ID_HEADER, required = false) String operatorId,
            @RequestBody ChannelCallbackOrderPageRequest request
    ) {
        validateTestToken(testToken);
        return HudsonResponse.success(
                channelCallbackOrderService.getPage(channelCode, request, parseOperatorUserId(operatorId)),
                TraceIdFactory.next("channel-callback-orders-page-get")
        );
    }

    @PostMapping("/channelCallbacks/{channelCode}/orders/detail/get")
    public HudsonResponse<OrderDetailAggregateVO> getOrderDetail(
            @PathVariable String channelCode,
            @RequestHeader(name = TEST_TOKEN_HEADER, required = false) String testToken,
            @RequestHeader(name = OPERATOR_ID_HEADER, required = false) String operatorId,
            @RequestBody ChannelCallbackOrderDetailRequest request
    ) {
        validateTestToken(testToken);
        return HudsonResponse.success(
                channelCallbackOrderService.getDetail(channelCode, request, parseOperatorUserId(operatorId)),
                TraceIdFactory.next("channel-callback-orders-detail-get")
        );
    }

    @PostMapping("/channelCallbacks/{channelCode}/orders/cancel")
    public HudsonResponse<ChannelCallbackOrderOperationResponseVO> cancelOrder(
            @PathVariable String channelCode,
            @RequestHeader(name = TEST_TOKEN_HEADER, required = false) String testToken,
            @RequestHeader(name = OPERATOR_ID_HEADER, required = false) String operatorId,
            @RequestBody ChannelCallbackOrderCancelRequest request
    ) {
        validateTestToken(testToken);
        return HudsonResponse.success(
                channelCallbackOrderService.cancel(channelCode, request, parseOperatorUserId(operatorId)),
                TraceIdFactory.next("channel-callback-orders-cancel")
        );
    }

    @PostMapping("/channelCallbacks/{channelCode}/orders/modify")
    public HudsonResponse<ChannelCallbackOrderOperationResponseVO> modifyOrder(
            @PathVariable String channelCode,
            @RequestHeader(name = TEST_TOKEN_HEADER, required = false) String testToken,
            @RequestHeader(name = OPERATOR_ID_HEADER, required = false) String operatorId,
            @RequestBody ChannelCallbackOrderModifyRequest request
    ) {
        validateTestToken(testToken);
        return HudsonResponse.success(
                channelCallbackOrderService.modify(channelCode, request, parseOperatorUserId(operatorId)),
                TraceIdFactory.next("channel-callback-orders-modify")
        );
    }

    @PostMapping("/channelCallbacks/{channelCode}/orders/status/sync")
    public HudsonResponse<ChannelCallbackOrderOperationResponseVO> syncOrderStatus(
            @PathVariable String channelCode,
            @RequestHeader(name = TEST_TOKEN_HEADER, required = false) String testToken,
            @RequestHeader(name = OPERATOR_ID_HEADER, required = false) String operatorId,
            @RequestBody ChannelCallbackOrderStatusSyncRequest request
    ) {
        validateTestToken(testToken);
        return HudsonResponse.success(
                channelCallbackOrderService.syncStatus(channelCode, request, parseOperatorUserId(operatorId)),
                TraceIdFactory.next("channel-callback-orders-status-sync")
        );
    }

    private void validateTestToken(String testToken) {
        if (channelCallbackTestToken == null
                || channelCallbackTestToken.isBlank()
                || testToken == null
                || !channelCallbackTestToken.equals(testToken)) {
            throw new BusinessException(401, AUTH_FAILED_MESSAGE);
        }
    }

    private Long parseOperatorUserId(String operatorId) {
        if (operatorId == null || operatorId.isBlank()) {
            throw new BusinessException(40001, "缺少第三方渠道操作人: " + OPERATOR_ID_HEADER);
        }
        try {
            return Long.valueOf(operatorId);
        } catch (NumberFormatException exception) {
            throw new BusinessException(40001, "第三方渠道操作人格式不正确: " + OPERATOR_ID_HEADER);
        }
    }
}
