package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.PoiPageRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.PoiService;
import com.jeez.zp.platform.vo.PoiPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PoiController {

    private final PoiService poiService;

    @PostMapping("/select/poi/page/get")
    public HudsonResponse<PoiPageResponseVO> getPage(@RequestBody PoiPageRequest request) {
        return HudsonResponse.success(
                poiService.getPage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLong(request.getChannelId()),
                        parseInteger(request.getIsAvailability()),
                        request.getPageNum() != null ? request.getPageNum() : request.getCurrent(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("poi-page-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Integer.valueOf(value);
    }
}
