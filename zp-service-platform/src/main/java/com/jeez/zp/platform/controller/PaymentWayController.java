package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.CampIdRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.PaymentWayService;
import com.jeez.zp.platform.vo.PaymentWaysResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentWayController {

    private final PaymentWayService paymentWayService;

    @PostMapping("/paymentWays/get")
    public HudsonResponse<PaymentWaysResponseVO> getPaymentWays(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                paymentWayService.getPaymentWays(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("payment-ways-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
