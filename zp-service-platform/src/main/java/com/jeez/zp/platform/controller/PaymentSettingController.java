package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.PaymentSettingRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.PaymentSettingService;
import com.jeez.zp.platform.vo.PaymentMethodVO;
import com.jeez.zp.platform.vo.PaymentSettingListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentSettingController {

    private final PaymentSettingService paymentSettingService;

    @PostMapping("/paymentSettings/list")
    public HudsonResponse<PaymentSettingListVO> getPaymentSettings(@RequestBody PaymentSettingRequest request) {
        return HudsonResponse.success(
                paymentSettingService.getPaymentSettings(
                        parseNullableLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getIncludeDisabled()
                ),
                TraceIdFactory.next("payment-settings-list")
        );
    }

    @PostMapping("/paymentSettings/detail")
    public HudsonResponse<PaymentMethodVO> getPaymentSettingDetail(@RequestBody PaymentSettingRequest request) {
        return HudsonResponse.success(
                paymentSettingService.getPaymentSettingDetail(
                        parseRequiredLong(request.getMethodId(), "methodId不能为空"),
                        LoginUserContext.requiredUserId()
                ),
                TraceIdFactory.next("payment-settings-detail")
        );
    }

    private Long parseNullableLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long parseRequiredLong(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return Long.valueOf(value);
    }
}
