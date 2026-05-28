package com.jeez.zp.room.service;

import com.jeez.zp.room.vo.RoomCategoryPageResponseVO;

public interface RoomCategoryService {

    RoomCategoryPageResponseVO getPage(
            Long campId,
            Long userId,
            Long poiId,
            Long roomCategoryGroupId,
            String roomCategoryName,
            String keyword,
            Long channelId,
            Integer pageNum,
            Integer pageSize
    );
}
