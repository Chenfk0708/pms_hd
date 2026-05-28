package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.RoomCategoryPricingRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.RoomCategoryPricingService;
import com.jeez.zp.platform.vo.RoomCategoryPricingTableResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoomCategoryPricingController {

    private final RoomCategoryPricingService roomCategoryPricingService;

    @PostMapping("/roomCategoryPricings/get")
    public HudsonResponse<RoomCategoryPricingTableResponseVO> getPricings(@RequestBody RoomCategoryPricingRequest request) {
        return HudsonResponse.success(
                roomCategoryPricingService.getPricings(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getRoomCategoryIds(),
                        request.getChannelIds()
                ),
                TraceIdFactory.next("room-category-pricings-get")
        );
    }

    @PostMapping("/roomCategoryRules/get")
    public HudsonResponse<RoomCategoryPricingTableResponseVO> getRules(@RequestBody RoomCategoryPricingRequest request) {
        return HudsonResponse.success(
                roomCategoryPricingService.getRules(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getRoomCategoryIds(),
                        request.getChannelIds(),
                        request.getDiscountType()
                ),
                TraceIdFactory.next("room-category-rules-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
