package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.RoomStatusesTodayResponseVO;

import java.util.List;

public interface RoomStatusesTodayService {

    RoomStatusesTodayResponseVO getRoomStatusesToday(
            Long campId,
            Long userId,
            List<Long> channelIds,
            List<Long> roomCategoryGroupIds,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            Object cleanStatus,
            Object date,
            Object queryCode,
            String storeId,
            String keyword,
            String viewMode,
            List<String> statusFilters,
            String channel,
            String roomType,
            String tag
    );
}
