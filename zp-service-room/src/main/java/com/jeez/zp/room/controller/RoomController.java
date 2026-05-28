package com.jeez.zp.room.controller;

import com.jeez.zp.room.api.HudsonResponse;
import com.jeez.zp.room.api.TraceIdFactory;
import com.jeez.zp.room.dto.request.RoomQueryRequest;
import com.jeez.zp.room.security.LoginUserContext;
import com.jeez.zp.room.service.RoomService;
import com.jeez.zp.room.vo.RoomCategoryRoomsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/rooms/get")
    public HudsonResponse<RoomCategoryRoomsResponseVO> getRooms(@RequestBody RoomQueryRequest request) {
        return HudsonResponse.success(
                roomService.getRooms(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLongList(request.getRoomCategoryIds()),
                        request.getSaleType()
                ),
                TraceIdFactory.next("rooms-get")
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
