package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.PrintSettingsGetRequest;
import com.jeez.zp.platform.dto.request.PrintSettingsSaveRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.PrintSettingService;
import com.jeez.zp.platform.vo.PrintSettingsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PrintSettingController {

    private final PrintSettingService printSettingService;

    @PostMapping("/printSettings/get")
    public HudsonResponse<PrintSettingsResponseVO> getPrintSettings(@RequestBody PrintSettingsGetRequest request) {
        return HudsonResponse.success(
                printSettingService.getPrintSettings(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("print-settings-get")
        );
    }

    @PostMapping("/printSettings/save")
    public HudsonResponse<PrintSettingsResponseVO> savePrintSettings(@RequestBody PrintSettingsSaveRequest request) {
        return HudsonResponse.success(
                printSettingService.savePrintSetting(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("print-settings-save")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
