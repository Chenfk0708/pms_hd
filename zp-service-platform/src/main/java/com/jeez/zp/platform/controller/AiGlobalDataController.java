package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.AiGlobalReminderPageRequest;
import com.jeez.zp.platform.dto.request.AiGlobalShopRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.AiGlobalDataService;
import com.jeez.zp.platform.vo.AiGlobalReminderPageResponseVO;
import com.jeez.zp.platform.vo.AiGlobalShopStatusVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AiGlobalDataController {

    private final AiGlobalDataService aiGlobalDataService;

    @PostMapping("/orders/strongReminder/page/get")
    public HudsonResponse<AiGlobalReminderPageResponseVO> getStrongReminderPage(@RequestBody AiGlobalReminderPageRequest request) {
        return HudsonResponse.success(
                aiGlobalDataService.getStrongReminderPage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getPage(),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("orders-strong-reminder-page-get")
        );
    }

    @PostMapping("/radarConfig/shop/get")
    public HudsonResponse<List<AiGlobalShopStatusVO>> getShopStatuses(@RequestBody AiGlobalShopRequest request) {
        return HudsonResponse.success(
                aiGlobalDataService.getShopStatuses(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getStatus()
                ),
                TraceIdFactory.next("radar-config-shop-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
