package com.jeez.zp.room.controller;

import com.jeez.zp.room.api.HudsonResponse;
import com.jeez.zp.room.api.TraceIdFactory;
import com.jeez.zp.room.dto.request.CampIdRequest;
import com.jeez.zp.room.dto.request.RoomCategoryPricingRequest;
import com.jeez.zp.room.security.LoginUserContext;
import com.jeez.zp.room.service.RoomCategoryPricingService;
import com.jeez.zp.room.vo.RoomCategoryPricingTableResponseVO;
import com.jeez.zp.room.vo.RetailSalePriceSettingVO;
import com.jeez.zp.room.vo.StoresPriceShowVO;
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

    @PostMapping("/roomCategoryPrice/salePriceSetting/get")
    public HudsonResponse<RetailSalePriceSettingVO> getSalePriceSetting(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                roomCategoryPricingService.getSalePriceSetting(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId()
                ),
                TraceIdFactory.next("room-category-price-sale-price-setting-get")
        );
    }

    @PostMapping("/systemConfig/price/storesPriceShow/get")
    public HudsonResponse<StoresPriceShowVO> getStoresPriceShow(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                roomCategoryPricingService.getStoresPriceShow(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId()
                ),
                TraceIdFactory.next("system-config-price-stores-price-show-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
