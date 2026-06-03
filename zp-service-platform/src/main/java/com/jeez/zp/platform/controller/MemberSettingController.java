package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.MemberSettingsBootstrapRequest;
import com.jeez.zp.platform.dto.request.MemberSettingsSaveRequest;
import com.jeez.zp.platform.dto.request.MemberWecomBindRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.MemberSettingService;
import com.jeez.zp.platform.vo.MemberSettingMemberVO;
import com.jeez.zp.platform.vo.MemberSettingsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberSettingController {

    private final MemberSettingService memberSettingService;

    @PostMapping("/memberSettings/bootstrap")
    public HudsonResponse<MemberSettingsResponseVO> bootstrap(@RequestBody MemberSettingsBootstrapRequest request) {
        return HudsonResponse.success(
                memberSettingService.bootstrap(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("member-settings-bootstrap")
        );
    }

    @PostMapping("/memberSettings/save")
    public HudsonResponse<MemberSettingsResponseVO> save(@RequestBody MemberSettingsSaveRequest request) {
        return HudsonResponse.success(
                membersResponse(memberSettingService.save(request, LoginUserContext.requiredUserId())),
                TraceIdFactory.next("member-settings-save")
        );
    }

    @PostMapping("/memberSettings/wecom/bind")
    public HudsonResponse<MemberSettingsResponseVO> bindWecom(@RequestBody MemberWecomBindRequest request) {
        return HudsonResponse.success(
                membersResponse(memberSettingService.bindWecom(request, LoginUserContext.requiredUserId())),
                TraceIdFactory.next("member-settings-wecom-bind")
        );
    }

    private MemberSettingsResponseVO membersResponse(List<MemberSettingMemberVO> members) {
        MemberSettingsResponseVO response = new MemberSettingsResponseVO();
        response.setMembers(members);
        return response;
    }
}
