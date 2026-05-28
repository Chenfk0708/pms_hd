package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.RoomCategoryDetailResponseVO;
import com.jeez.zp.platform.vo.RoomCategoryPageResponseVO;

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

    RoomCategoryDetailResponseVO getDetail(Long roomCategoryId, Long userId);
}
