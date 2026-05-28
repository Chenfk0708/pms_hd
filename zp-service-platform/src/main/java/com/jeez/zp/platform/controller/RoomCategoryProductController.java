package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.RoomCategoryProductPageRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.RoomCategoryProductService;
import com.jeez.zp.platform.vo.RoomCategoryProductPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
