package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.SmsTemplateMsgConfigPageRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.SmsTemplateMsgConfigService;
import com.jeez.zp.platform.vo.SmsTemplateMsgConfigPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SmsTemplateMsgConfigController {

    private final SmsTemplateMsgConfigService smsTemplateMsgConfigService;

    @PostMapping("/smsTemplateMsgConfig/page/get")
    public HudsonResponse<SmsTemplateMsgConfigPageResponseVO> getPage(@RequestBody SmsTemplateMsgConfigPageRequest request) {
        return HudsonResponse.success(
                smsTemplateMsgConfigService.getPage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getSendType(),
                        request.getPageNum(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("sms-template-msg-config-page-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
