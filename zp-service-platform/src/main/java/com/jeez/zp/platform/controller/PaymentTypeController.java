package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.CampIdRequest;
import com.jeez.zp.platform.dto.request.PaymentTypeV2Request;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.PaymentTypeService;
import com.jeez.zp.platform.vo.PaymentTypeGroupsResponseVO;
import com.jeez.zp.platform.vo.PaymentTypesResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentTypeController {

    private final PaymentTypeService paymentTypeService;

    @PostMapping("/paymentTypes/get")
    public HudsonResponse<PaymentTypesResponseVO> getPaymentTypes(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                paymentTypeService.getPaymentTypes(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("payment-types-get")
        );
    }

    @PostMapping("/paymentTypes/get/v2")
    public HudsonResponse<PaymentTypeGroupsResponseVO> getPaymentTypesV2(@RequestBody PaymentTypeV2Request request) {
        return HudsonResponse.success(
                paymentTypeService.getPaymentTypesV2(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getBizTypes(),
                        request.getIsEnable()
                ),
                TraceIdFactory.next("payment-types-get-v2")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
