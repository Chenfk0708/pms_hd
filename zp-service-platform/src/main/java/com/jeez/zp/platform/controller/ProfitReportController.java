package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.ProfitReportRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.ProfitReportService;
import com.jeez.zp.platform.vo.ProfitReportPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProfitReportController {

    private final ProfitReportService profitReportService;

    @PostMapping("/report/profit/get/v2")
    public HudsonResponse<ProfitReportPageResponseVO> getProfitReport(@RequestBody ProfitReportRequest request) {
        return HudsonResponse.success(
                profitReportService.getProfitReport(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getStartDate(),
                        request.getEndDate(),
                        parseLong(request.getPoiId()),
                        parseLong(request.getRoomCategoryId()),
                        parseLong(request.getRoomCategoryGroupId()),
                        parseLong(request.getChannelId()),
                        parseLong(request.getRoomId()),
                        request.getIsCleanCost(),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("report-profit-get-v2")
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
