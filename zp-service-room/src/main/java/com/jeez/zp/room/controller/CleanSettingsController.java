package com.jeez.zp.room.controller;

import com.jeez.zp.room.api.HudsonResponse;
import com.jeez.zp.room.api.TraceIdFactory;
import com.jeez.zp.room.dto.request.CleanSettingsBootstrapRequest;
import com.jeez.zp.room.security.LoginUserContext;
import com.jeez.zp.room.service.CleanSettingService;
import com.jeez.zp.room.vo.CleanSettingBootstrapResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CleanSettingsController {

    private final CleanSettingService cleanSettingService;

    @PostMapping("/cleanSettings/bootstrap")
    public HudsonResponse<CleanSettingBootstrapResponseVO> bootstrap(@RequestBody CleanSettingsBootstrapRequest request) {
        return HudsonResponse.success(
                cleanSettingService.bootstrap(
                        parseLongOrNull(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getBusinessDate(),
                        request.getStoreId(),
                        request.getProjectId(),
                        request.getStatus(),
                        request.getPage(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("clean-settings-bootstrap")
        );
    }

    private Long parseLongOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
