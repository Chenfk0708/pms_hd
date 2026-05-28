package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.CampIdRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.DepositChannelService;
import com.jeez.zp.platform.vo.DepositChannelOptionsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DepositChannelController {

    private final DepositChannelService depositChannelService;

    @PostMapping("/select/calChannel4Deposit/get")
    public HudsonResponse<DepositChannelOptionsResponseVO> getDepositChannels(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                depositChannelService.getDepositChannels(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("deposit-channels-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
