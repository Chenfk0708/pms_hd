package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.DistributionFlowPageRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.DistributionFlowService;
import com.jeez.zp.platform.vo.DistributionFlowPageResponseVO;
import com.jeez.zp.platform.vo.DistributionOrderPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DistributionFlowController {

    private final DistributionFlowService distributionFlowService;

    @PostMapping("/report/flows/get")
    public HudsonResponse<DistributionFlowPageResponseVO> getPage(@RequestBody DistributionFlowPageRequest request) {
        return HudsonResponse.success(
                distributionFlowService.getPage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize(),
                        request.getBookingStartDate(),
                        request.getBookingEndDate(),
                        request.getKeyword(),
                        request.getBreakTemp(),
                        request.getSettledState()
                ),
                TraceIdFactory.next("report-flows-get")
        );
    }

    @PostMapping("/distribution/orders/page/get")
    public HudsonResponse<DistributionOrderPageResponseVO> getDistributionOrdersPage(@RequestBody DistributionFlowPageRequest request) {
        return HudsonResponse.success(
                distributionFlowService.getDistributionOrdersPage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize(),
                        request.getBookingStartDate(),
                        request.getBookingEndDate(),
                        request.getKeyword(),
                        request.getSettledState()
                ),
                TraceIdFactory.next("distribution-orders-page-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
