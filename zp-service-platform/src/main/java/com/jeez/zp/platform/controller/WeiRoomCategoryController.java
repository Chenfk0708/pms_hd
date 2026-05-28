package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.WeiRoomCategoryPageRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.WeiRoomCategoryService;
import com.jeez.zp.platform.vo.WeiRoomCategoryPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WeiRoomCategoryController {

    private final WeiRoomCategoryService weiRoomCategoryService;

    @PostMapping("/weiRoomCategories/page/get")
    public HudsonResponse<WeiRoomCategoryPageResponseVO> getPage(@RequestBody WeiRoomCategoryPageRequest request) {
        return HudsonResponse.success(
                weiRoomCategoryService.getPage(
                        parseLong(request.getCampId()),
                        parseLong(request.getBuyCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getRoomCategoryTypes(),
                        request.getGoodsTypes(),
                        request.getPageNum(),
                        request.getPageSize(),
                        request.getKeyword()
                ),
                TraceIdFactory.next("wei-room-categories-page-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
