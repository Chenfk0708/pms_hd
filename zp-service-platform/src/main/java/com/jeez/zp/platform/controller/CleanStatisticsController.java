package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.CleanTaskStatisticsRequest;
import com.jeez.zp.platform.dto.request.CleanerListRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.CleanStatisticsService;
import com.jeez.zp.platform.vo.CleanTaskStatisticsResponseVO;
import com.jeez.zp.platform.vo.CleanerListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CleanStatisticsController {

    private final CleanStatisticsService cleanStatisticsService;

    @PostMapping("/cleanTask/statistics")
    public HudsonResponse<CleanTaskStatisticsResponseVO> getStatistics(@RequestBody CleanTaskStatisticsRequest request) {
        return HudsonResponse.success(
                cleanStatisticsService.getStatistics(
                        parseLongOrNull(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLongOrNull(request.getStoreId()),
                        request.getRoomIds(),
                        request.getCleanerIds(),
                        request.getCleanStartTime(),
                        request.getCleanEndTime(),
                        request.getPageNum(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("clean-task-statistics")
        );
    }

    @PostMapping("/cleaner/list/get")
    public HudsonResponse<List<CleanerListItemVO>> getCleaners(@RequestBody CleanerListRequest request) {
        return HudsonResponse.success(
                cleanStatisticsService.getCleaners(
                        parseLongOrNull(request.getCampId()),
                        LoginUserContext.requiredUserId()
                ),
                TraceIdFactory.next("cleaner-list-get")
        );
    }

    private Long parseLongOrNull(String value) {
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
