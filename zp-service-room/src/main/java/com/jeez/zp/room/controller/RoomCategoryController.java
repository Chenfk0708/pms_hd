package com.jeez.zp.room.controller;

import com.jeez.zp.room.api.HudsonResponse;
import com.jeez.zp.room.api.TraceIdFactory;
import com.jeez.zp.room.dto.request.RoomCategoryPageRequest;
import com.jeez.zp.room.security.LoginUserContext;
import com.jeez.zp.room.service.RoomCategoryService;
import com.jeez.zp.room.vo.RoomCategoryPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoomCategoryController {

    private final RoomCategoryService roomCategoryService;

    @PostMapping("/roomCategories/page/get")
    public HudsonResponse<RoomCategoryPageResponseVO> getPage(@RequestBody RoomCategoryPageRequest request) {
        return HudsonResponse.success(
                roomCategoryService.getPage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLong(request.getPoiId()),
                        parseLong(request.getRoomCategoryGroupId()),
                        request.getRoomCategoryName(),
                        request.getKeyword(),
                        normalizeZeroToNull(parseLong(request.getChannelId())),
                        request.getPageNum(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("room-categories-page-get")
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

    private Long normalizeZeroToNull(Long value) {
        if (value == null || value == 0L) {
            return null;
        }
        return value;
    }
}
