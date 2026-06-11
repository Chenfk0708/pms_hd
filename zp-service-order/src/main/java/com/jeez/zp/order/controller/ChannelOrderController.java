package com.jeez.zp.order.controller;

import com.jeez.zp.order.api.HudsonResponse;
import com.jeez.zp.order.api.TraceIdFactory;
import com.jeez.zp.order.dto.request.ChannelOrderImportRequest;
import com.jeez.zp.order.security.LoginUserContext;
import com.jeez.zp.order.service.ChannelOrderImportService;
import com.jeez.zp.order.vo.ChannelOrderImportResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChannelOrderController {

    private final ChannelOrderImportService channelOrderImportService;

    @PostMapping("/channelOrders/import")
    public HudsonResponse<ChannelOrderImportResponseVO> importOrder(@RequestBody ChannelOrderImportRequest request) {
        return HudsonResponse.success(
                channelOrderImportService.importOrder(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("channel-orders-import")
        );
    }
}
