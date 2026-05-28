package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.DailyRoomStatusRequest;
import com.jeez.zp.platform.dto.request.ForwardRoomStatusRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.RoomSituationReportService;
import com.jeez.zp.platform.vo.RoomSituationPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomSituationReportController {

    private final RoomSituationReportService roomSituationReportService;

    @PostMapping("/report/dailyRoomStatus/get")
    public HudsonResponse<RoomSituationPageResponseVO> getDailyRoomStatus(@RequestBody DailyRoomStatusRequest request) {
        return HudsonResponse.success(
                roomSituationReportService.getDailyRoomStatus(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getDate(),
                        parseLongList(request.getPoiIds()),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("report-daily-room-status-get")
        );
    }

    @PostMapping("/report/forwardRoomStatus/get")
    public HudsonResponse<RoomSituationPageResponseVO> getForwardRoomStatus(@RequestBody ForwardRoomStatusRequest request) {
        return HudsonResponse.success(
                roomSituationReportService.getForwardRoomStatus(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getStartDate(),
                        request.getEndDate(),
                        parseLongList(request.getPoiIds()),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("report-forward-room-status-get")
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
                .filter(value -> value != null && !value.isBlank())
                .map(this::parseLong)
                .filter(value -> value != null)
                .toList();
    }
}
