package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.CampIdRequest;
import com.jeez.zp.platform.dto.request.SystemConfigMutationRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.SystemConfigService;
import com.jeez.zp.platform.vo.SystemConfigItemVO;
import com.jeez.zp.platform.vo.SystemConfigsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @PostMapping("/systemConfig/checkInGuideShowStrategy/get")
    public HudsonResponse<SystemConfigItemVO> getCheckInGuideShowStrategy(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                systemConfigService.getCheckInGuideShowStrategy(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId()
                ),
                TraceIdFactory.next("system-config-check-in-guide-show-get")
        );
    }

    @PostMapping("/systemConfig/orderAutoPendingStrategy")
    public HudsonResponse<SystemConfigsResponseVO> updateOrderAutoPendingStrategy(@RequestBody SystemConfigMutationRequest request) {
        return HudsonResponse.success(
                systemConfigService.updateOrderAutoPendingStrategy(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getConfigKey(),
                        request.getConfigValue()
                ),
                TraceIdFactory.next("system-config-order-auto-pending-update")
        );
    }

    @PostMapping("/systemConfig/orderAutoSettleStrategy")
    public HudsonResponse<SystemConfigsResponseVO> updateOrderAutoSettleStrategy(@RequestBody SystemConfigMutationRequest request) {
        return HudsonResponse.success(
                systemConfigService.updateOrderAutoSettleStrategy(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getConfigKey(),
                        request.getConfigValue()
                ),
                TraceIdFactory.next("system-config-order-auto-settle-update")
        );
    }

    @PostMapping("/systemConfig/negotiateRefundAutomaticAcceptStrategy")
    public HudsonResponse<SystemConfigsResponseVO> updateNegotiateRefundAutomaticAcceptStrategy(@RequestBody SystemConfigMutationRequest request) {
        return HudsonResponse.success(
                systemConfigService.updateNegotiateRefundAutomaticAcceptStrategy(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getConfigKey(),
                        request.getConfigValue()
                ),
                TraceIdFactory.next("system-config-negotiate-refund-update")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
