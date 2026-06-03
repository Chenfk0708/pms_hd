package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.EditionReplaceOrderRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.EditionReplaceOrderService;
import com.jeez.zp.platform.vo.EditionReplaceOrderPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EditionReplaceOrderController {

    private final EditionReplaceOrderService editionReplaceOrderService;

    @PostMapping("/edition/replace/order/get")
    public HudsonResponse<EditionReplaceOrderPageResponseVO> getReplaceOrders(@RequestBody EditionReplaceOrderRequest request) {
        return HudsonResponse.success(
                editionReplaceOrderService.getReplaceOrders(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getReceiverStartTime(),
                        request.getReceiverEndTime(),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("edition-replace-order-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
