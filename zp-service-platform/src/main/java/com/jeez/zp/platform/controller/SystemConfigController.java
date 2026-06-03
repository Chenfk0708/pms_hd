package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.CampIdRequest;
import com.jeez.zp.platform.dto.request.CommonsRequest;
import com.jeez.zp.platform.dto.request.UserShortcutGetRequest;
import com.jeez.zp.platform.dto.request.SystemConfigMutationRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.SystemConfigService;
import com.jeez.zp.platform.vo.SystemConfigItemVO;
import com.jeez.zp.platform.vo.SystemConfigsResponseVO;
import com.jeez.zp.platform.vo.CommonsResponseVO;
import com.jeez.zp.platform.vo.UserShortcutResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PostMapping("/systemConfig/checkWifiShowStrategy/get")
    public HudsonResponse<SystemConfigItemVO> getCheckWifiShowStrategy(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                systemConfigService.getCheckWifiShowStrategy(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId()
                ),
                TraceIdFactory.next("system-config-check-wifi-show-get")
        );
    }

    @PutMapping("/systemConfig/checkInGuideShowStrategy")
    public HudsonResponse<SystemConfigsResponseVO> updateCheckInGuideShowStrategy(@RequestBody SystemConfigMutationRequest request) {
        return HudsonResponse.success(
                systemConfigService.updateCheckInGuideShowStrategy(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getIsCheckInGuideIdentityRegCompleted(),
                        request.getIsCheckInGuideVerifyPayDeposit()
                ),
                TraceIdFactory.next("system-config-check-in-guide-show-update")
        );
    }

    @PutMapping("/systemConfig/checkWifiShowStrategy")
    public HudsonResponse<SystemConfigsResponseVO> updateCheckWifiShowStrategy(@RequestBody SystemConfigMutationRequest request) {
        return HudsonResponse.success(
                systemConfigService.updateCheckWifiShowStrategy(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getIsWifiDisplayEnabled()
                ),
                TraceIdFactory.next("system-config-check-wifi-show-update")
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

    @PostMapping("/systemConfigs/nightAudit/save")
    public HudsonResponse<SystemConfigsResponseVO> updateNightAudit(@RequestBody SystemConfigMutationRequest request) {
        return HudsonResponse.success(
                systemConfigService.updateNightAudit(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getIsNightAudit(),
                        request.getAutoNightAuditTime()
                ),
                TraceIdFactory.next("system-configs-night-audit-save")
        );
    }

    @PostMapping("/systemConfigs/financeStrategy/save")
    public HudsonResponse<SystemConfigsResponseVO> updateFinanceStrategy(@RequestBody SystemConfigMutationRequest request) {
        return HudsonResponse.success(
                systemConfigService.updateFinanceStrategy(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getOrderAmortizeStrategy()
                ),
                TraceIdFactory.next("system-configs-finance-strategy-save")
        );
    }

    @PostMapping("/systemConfigs/vendibleTypes/save")
    public HudsonResponse<SystemConfigsResponseVO> updateVendibleTypes(@RequestBody SystemConfigMutationRequest request) {
        return HudsonResponse.success(
                systemConfigService.updateVendibleTypes(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getVendibleTypes()
                ),
                TraceIdFactory.next("system-configs-vendible-types-save")
        );
    }

    @PostMapping("/commons/get")
    public HudsonResponse<CommonsResponseVO> getCommons(@RequestBody CommonsRequest request) {
        return HudsonResponse.success(
                systemConfigService.getCommons(
                        parseLong(request == null ? null : request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request == null ? null : request.getCode()
                ),
                TraceIdFactory.next("commons-get")
        );
    }

    @PostMapping("/systemConfigs/user/shortcut/get")
    public HudsonResponse<UserShortcutResponseVO> getUserShortcuts(@RequestBody UserShortcutGetRequest request) {
        return HudsonResponse.success(
                systemConfigService.getUserShortcuts(
                        parseLong(request == null ? null : request.getUserId()),
                        LoginUserContext.requiredUserId()
                ),
                TraceIdFactory.next("system-configs-user-shortcut-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
