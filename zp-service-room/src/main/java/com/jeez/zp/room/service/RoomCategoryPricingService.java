package com.jeez.zp.room.service;

import com.jeez.zp.room.vo.RoomCategoryPricingTableResponseVO;
import com.jeez.zp.room.vo.RetailSalePriceSettingVO;
import com.jeez.zp.room.vo.StoresPriceShowVO;

import java.util.List;

public interface RoomCategoryPricingService {

    RoomCategoryPricingTableResponseVO getPricings(
            Long campId,
            Long userId,
            List<String> roomCategoryIds,
            List<String> channelIds
    );

    RoomCategoryPricingTableResponseVO getRules(
            Long campId,
            Long userId,
            List<String> roomCategoryIds,
            List<String> channelIds,
            Integer discountType
    );

    RetailSalePriceSettingVO getSalePriceSetting(Long campId, Long userId);

    StoresPriceShowVO getStoresPriceShow(Long campId, Long userId);
}
