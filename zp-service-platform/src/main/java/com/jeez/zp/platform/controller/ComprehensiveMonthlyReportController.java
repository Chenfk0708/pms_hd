package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.ComprehensiveMonthlyReportPageRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.ComprehensiveMonthlyReportService;
import com.jeez.zp.platform.vo.ComprehensiveMonthlyReportPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ComprehensiveMonthlyReportController {

    private final ComprehensiveMonthlyReportService comprehensiveMonthlyReportService;

    @PostMapping("/report/monthly/page/get")
    public HudsonResponse<ComprehensiveMonthlyReportPageResponseVO> getPage(@RequestBody ComprehensiveMonthlyReportPageRequest request) {
        return HudsonResponse.success(
                comprehensiveMonthlyReportService.getPage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getPage(),
                        request.getPageNum(),
                        request.getPageSize(),
                        request.getCurrent()
                ),
                TraceIdFactory.next("report-monthly-page-get")
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
