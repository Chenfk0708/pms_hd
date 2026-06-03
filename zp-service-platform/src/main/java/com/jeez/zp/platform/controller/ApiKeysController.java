package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.CampIdRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.ApiKeysService;
import com.jeez.zp.platform.vo.ApiKeysPayloadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ApiKeysController {

    private final ApiKeysService apiKeysService;

    @PostMapping("/user/secret/get")
    public HudsonResponse<ApiKeysPayloadVO> get(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                apiKeysService.get(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("user-secret-get")
        );
    }

    @PostMapping("/user/secret/generate")
    public HudsonResponse<ApiKeysPayloadVO> generate(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                apiKeysService.generate(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("user-secret-generate")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
