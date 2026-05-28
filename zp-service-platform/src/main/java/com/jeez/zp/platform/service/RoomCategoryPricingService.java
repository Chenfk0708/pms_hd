package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.RoomCategoryPricingTableResponseVO;

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
}
