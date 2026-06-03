package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.OtaChannelDetailRequest;
import com.jeez.zp.platform.dto.request.OtaDashboardRequest;
import com.jeez.zp.platform.dto.request.OtaLogPageRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.OtaService;
import com.jeez.zp.platform.vo.OtaChannelDetailVO;
import com.jeez.zp.platform.vo.OtaDashboardVO;
import com.jeez.zp.platform.vo.OtaLogPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OtaController {

    private final OtaService otaService;

    @PostMapping("/ota/dashboard/get")
    public HudsonResponse<OtaDashboardVO> getDashboard(@RequestBody OtaDashboardRequest request) {
        return HudsonResponse.success(
                otaService.getDashboard(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("ota-dashboard-get")
        );
    }

    @PostMapping("/ota/channel/detail/get")
    public HudsonResponse<OtaChannelDetailVO> getChannelDetail(@RequestBody OtaChannelDetailRequest request) {
        return HudsonResponse.success(
                otaService.getChannelDetail(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("ota-channel-detail-get")
        );
    }

    @PostMapping("/ota/log/page/get")
    public HudsonResponse<OtaLogPageVO> getLogPage(@RequestBody OtaLogPageRequest request) {
        return HudsonResponse.success(
                otaService.getLogPage(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("ota-log-page-get")
        );
    }
}
