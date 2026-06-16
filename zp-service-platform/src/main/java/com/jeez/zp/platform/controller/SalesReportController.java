package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.SalesReportRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.SalesReportService;
import com.jeez.zp.platform.vo.SalesReportPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class SalesReportController {

    private final SalesReportService salesReportService;

    @PostMapping("/report/open/room/get")
    public HudsonResponse<SalesReportPageResponseVO> getSalesReport(@RequestBody SalesReportRequest request) {
        return HudsonResponse.success(
                salesReportService.getSalesReport(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getStartDate(),
                        request.getEndDate(),
                        parseLongList(request.getPoiIds()),
                        parseLongList(request.getRoomCategoryIds()),
                        parseLongList(request.getRoomCategoryGroupIds()),
                        parseLongList(request.getChannelIds()),
                        parseLongList(request.getRoomIds()),
                        request.getQueryType(),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("report-open-room-get")
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

    private List<Long> parseLongList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .map(this::parseLong)
                .filter(Objects::nonNull)
                .toList();
    }
}
