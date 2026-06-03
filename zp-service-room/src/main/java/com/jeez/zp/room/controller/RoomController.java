package com.jeez.zp.room.controller;

import com.jeez.zp.room.api.HudsonResponse;
import com.jeez.zp.room.api.TraceIdFactory;
import com.jeez.zp.room.dto.request.RoomPageRequest;
import com.jeez.zp.room.dto.request.RoomQueryRequest;
import com.jeez.zp.room.dto.request.RoomStatusesRoomsRequest;
import com.jeez.zp.room.security.LoginUserContext;
import com.jeez.zp.room.service.RoomService;
import com.jeez.zp.room.vo.RoomCategoryRoomsResponseVO;
import com.jeez.zp.room.vo.RoomPageResponseVO;
import com.jeez.zp.room.vo.RoomStatusesRoomsResponseVO;
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

    @PostMapping("/rooms/page/get")
    public HudsonResponse<RoomPageResponseVO> getRoomsPage(@RequestBody RoomPageRequest request) {
        return HudsonResponse.success(
                roomService.getRoomsPage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLong(request.getPoiId()),
                        parseLong(request.getStoreId()),
                        parseLongList(request.getRoomCategoryIds()),
                        request.getIsAvailability(),
                        request.getSaleType(),
                        request.getKeyword(),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("rooms-page-get")
        );
    }

    @PostMapping("/roomStatuses/rooms/get")
    public HudsonResponse<RoomStatusesRoomsResponseVO> getRoomStatusesRooms(@RequestBody RoomStatusesRoomsRequest request) {
        return HudsonResponse.success(
                roomService.getRoomStatusesRooms(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLongList(request.getRoomCategoryIds()),
                        parseLongList(request.getPoiIds()),
                        parseLong(request.getStoreId()),
                        request.getQueryCode(),
                        request.getPage(),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("room-statuses-rooms-get")
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
