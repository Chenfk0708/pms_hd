package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.ChannelRoomCategoryPageRequest;
import com.jeez.zp.platform.dto.request.RoomCategoryProductPageRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.RoomCategoryProductService;
import com.jeez.zp.platform.vo.ChannelRoomCategoryPageResponseVO;
import com.jeez.zp.platform.vo.RoomCategoryProductPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomCategoryProductController {

    private final RoomCategoryProductService roomCategoryProductService;

    @PostMapping("/roomCategoryProducts/page/get")
    public HudsonResponse<RoomCategoryProductPageResponseVO> getPage(
            @RequestBody RoomCategoryProductPageRequest request
    ) {
        return HudsonResponse.success(
                roomCategoryProductService.getPage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getKeyword(),
                        parseLong(request.getRoomCategoryId()),
                        parseLong(request.getChannelId()),
                        request.getPageNum(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("room-category-products-page-get")
        );
    }


    @PostMapping("/channelRoomCategories/page/get/v2")
    public HudsonResponse<ChannelRoomCategoryPageResponseVO> getChannelRoomCategoriesPageV2(
            @RequestBody ChannelRoomCategoryPageRequest request
    ) {
        return HudsonResponse.success(
                roomCategoryProductService.getChannelRoomCategoryPageV2(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getRoomCategoryTypes(),
                        parseLongList(request.getCategoryIds()),
                        request.getSearchKey() == null ? request.getKeyword() : request.getSearchKey(),
                        request.getChannelIds(),
                        parseLongList(request.getPoiIds()),
                        request.getShelfStatuses(),
                        request.getPageNum(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("channel-room-categories-page-get-v2")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }

    private List<Long> parseLongList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(Long::valueOf)
                .toList();
    }

}
