package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.RoomStatusesTodayRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.RoomStatusesTodayService;
import com.jeez.zp.platform.vo.RoomStatusesTodayResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomStatusesTodayController {

    private final RoomStatusesTodayService roomStatusesTodayService;

    @PostMapping("/roomStatusesToday/get")
    public HudsonResponse<RoomStatusesTodayResponseVO> getRoomStatusesToday(@RequestBody RoomStatusesTodayRequest request) {
        return HudsonResponse.success(
                roomStatusesTodayService.getRoomStatusesToday(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLongList(request.getChannelIds()),
                        parseLongList(request.getRoomCategoryGroupIds()),
                        parseLongList(request.getRoomCategoryIds()),
                        parseLongList(request.getPoiIds()),
                        request.getCleanStatus(),
                        request.getDate(),
                        request.getQueryCode(),
                        request.getStoreId(),
                        request.getKeyword(),
                        request.getViewMode(),
                        request.getStatusFilters(),
                        request.getChannel(),
                        request.getRoomType(),
                        request.getTag()
                ),
                TraceIdFactory.next("room-statuses-today-get")
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
