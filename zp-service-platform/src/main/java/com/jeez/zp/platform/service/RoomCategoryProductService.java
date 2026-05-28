package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.RoomCategoryProductPageResponseVO;

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
}
