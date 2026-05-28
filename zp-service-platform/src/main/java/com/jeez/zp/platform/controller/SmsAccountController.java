package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.CampIdRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.SmsAccountService;
import com.jeez.zp.platform.vo.SmsAccountSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SmsAccountController {

    private final SmsAccountService smsAccountService;

    @PostMapping("/smsAccount/get")
    public HudsonResponse<SmsAccountSummaryVO> getSmsAccount(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                smsAccountService.getSmsAccountSummary(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("sms-account-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
