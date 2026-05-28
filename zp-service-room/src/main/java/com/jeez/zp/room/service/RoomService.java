package com.jeez.zp.room.service;

import com.jeez.zp.room.vo.RoomCategoryRoomsResponseVO;

import java.util.List;

public interface RoomService {

    RoomCategoryRoomsResponseVO getRooms(
            Long campId,
            Long userId,
            List<Long> roomCategoryIds,
            Integer saleType
    );
}
