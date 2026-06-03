package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.ChannelRoomCategoryPageResponseVO;
import com.jeez.zp.platform.vo.RoomCategoryProductPageResponseVO;

import java.util.List;

public interface RoomCategoryProductService {

    RoomCategoryProductPageResponseVO getPage(
            Long campId,
            Long userId,
            String keyword,
            Long roomCategoryId,
            Long channelId,
            Integer pageNum,
            Integer pageSize
    );

    ChannelRoomCategoryPageResponseVO getChannelRoomCategoryPageV2(
            Long campId,
            Long userId,
            List<Integer> roomCategoryTypes,
            List<Long> categoryIds,
            String keyword,
            List<Long> channelIds,
            List<Long> poiIds,
            List<String> shelfStatuses,
            Integer pageNum,
            Integer pageSize
    );
}
