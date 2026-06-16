package com.jeez.zp.room.controller;

import com.jeez.zp.room.api.HudsonResponse;
import com.jeez.zp.room.api.TraceIdFactory;
import com.jeez.zp.room.dto.request.CleanSettingRuleSaveRequest;
import com.jeez.zp.room.dto.request.CleanSettingsBootstrapRequest;
import com.jeez.zp.room.security.LoginUserContext;
import com.jeez.zp.room.service.CleanSettingService;
import com.jeez.zp.room.vo.CleanSettingBootstrapResponseVO;
import com.jeez.zp.room.vo.CleanSettingExportResponseVO;
import com.jeez.zp.room.vo.CleanSettingRuleSaveResponseVO;
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

    @PostMapping("/cleanManage/cleanSetting/overview")
    public HudsonResponse<CleanSettingBootstrapResponseVO> overview(@RequestBody CleanSettingsBootstrapRequest request) {
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
                TraceIdFactory.next("clean-setting-overview")
        );
    }

    @PostMapping({"/cleanSettings/rule/save", "/cleanManage/cleanSetting/rule/save"})
    public HudsonResponse<CleanSettingRuleSaveResponseVO> saveRule(@RequestBody CleanSettingRuleSaveRequest request) {
        return HudsonResponse.success(
                cleanSettingService.savePolicyRule(
                        parseLongOrNull(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getRule()
                ),
                TraceIdFactory.next("clean-setting-rule-save")
        );
    }

    @PostMapping({"/cleanSettings/export", "/cleanManage/cleanSetting/export"})
    public HudsonResponse<CleanSettingExportResponseVO> export(@RequestBody CleanSettingsBootstrapRequest request) {
        return HudsonResponse.success(
                cleanSettingService.export(
                        parseLongOrNull(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getBusinessDate(),
                        request.getStoreId(),
                        request.getProjectId(),
                        request.getStatus()
                ),
                TraceIdFactory.next("clean-setting-export")
        );
    }

    private Long parseLongOrNull(String value) {
        if (value == null || value.isBlank() || "all".equals(value) || "ALL".equals(value)) {
            return null;
        }
        return Long.valueOf(value);
    }
}
