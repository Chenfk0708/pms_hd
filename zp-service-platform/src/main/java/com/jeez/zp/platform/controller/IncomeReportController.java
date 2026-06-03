package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.IncomeReportRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.IncomeReportService;
import com.jeez.zp.platform.vo.IncomeReportPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class IncomeReportController {

    private final IncomeReportService incomeReportService;

    @PostMapping("/report/accommodation/get")
    public HudsonResponse<IncomeReportPageResponseVO> getIncomeReport(@RequestBody IncomeReportRequest request) {
        return HudsonResponse.success(
                incomeReportService.getIncomeReport(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getStartDate(),
                        request.getEndDate(),
                        parseLong(request.getPoiId()),
                        parseLong(request.getRoomCategoryId()),
                        parseLong(request.getRoomCategoryGroupId()),
                        parseLong(request.getChannelId()),
                        parseLong(request.getRoomId()),
                        request.getQueryType(),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("report-accommodation-get")
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
