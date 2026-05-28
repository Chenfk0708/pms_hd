package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.RoomCategoryRoomsResponseVO;

import java.util.List;

public interface RoomService {

    RoomCategoryRoomsResponseVO getRooms(
            Long campId,
            Long userId,
            List<Long> roomCategoryIds,
            Integer saleType
    );
}
