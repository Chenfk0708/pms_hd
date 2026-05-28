package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.CampIdRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.OrderChannelService;
import com.jeez.zp.platform.vo.OrderChannelOptionsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderChannelController {

    private final OrderChannelService orderChannelService;

    @PostMapping("/select/calChannel4Order/get")
    public HudsonResponse<OrderChannelOptionsResponseVO> getOrderChannels(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                orderChannelService.getOrderChannels(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("order-channels-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
