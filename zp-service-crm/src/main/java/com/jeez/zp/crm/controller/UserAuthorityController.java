package com.jeez.zp.crm.controller;

import com.jeez.zp.crm.api.HudsonResponse;
import com.jeez.zp.crm.api.TraceIdFactory;
import com.jeez.zp.crm.dto.request.AuthorityExcludeRequest;
import com.jeez.zp.crm.dto.request.CampRequest;
import com.jeez.zp.crm.security.LoginUserContext;
import com.jeez.zp.crm.service.AuthorityService;
import com.jeez.zp.crm.vo.NotificationAuthorityResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserAuthorityController {

    private final AuthorityService authorityService;

    @PostMapping("/userAuthority/notification/get")
    public HudsonResponse<NotificationAuthorityResponseVO> getNotifications(@RequestBody CampRequest request) {
        return HudsonResponse.success(
                authorityService.getNotifications(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("user-authority-notification-get")
        );
    }

    @PostMapping("/userAuthority/exclude")
    public HudsonResponse<Boolean> exclude(@RequestBody AuthorityExcludeRequest request) {
        return HudsonResponse.success(
                authorityService.exclude(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("user-authority-exclude")
        );
    }

    @DeleteMapping("/userAuthority/exclude")
    public HudsonResponse<Boolean> restore(@RequestBody AuthorityExcludeRequest request) {
        return HudsonResponse.success(
                authorityService.restore(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("user-authority-exclude-restore")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
