package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.WeiRoomCategoryPageResponseVO;

import java.util.List;

public interface WeiRoomCategoryService {

    WeiRoomCategoryPageResponseVO getPage(
            Long catalogCampId,
            Long buyCampId,
            Long userId,
            List<Integer> roomCategoryTypes,
            List<Integer> goodsTypes,
            Integer pageNum,
            Integer pageSize,
            String keyword
    );
}
