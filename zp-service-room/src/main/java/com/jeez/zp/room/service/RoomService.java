package com.jeez.zp.room.service;

import com.jeez.zp.room.vo.RoomCategoryRoomsResponseVO;
import com.jeez.zp.room.vo.RoomPageResponseVO;
import com.jeez.zp.room.vo.RoomStatusesRoomsResponseVO;

import java.util.List;

public interface RoomService {

    RoomCategoryRoomsResponseVO getRooms(
            Long campId,
            Long userId,
            List<Long> roomCategoryIds,
            Integer saleType
    );

    RoomPageResponseVO getRoomsPage(
            Long campId,
            Long userId,
            Long poiId,
            Long storeId,
            List<Long> roomCategoryIds,
            Integer isAvailability,
            Integer saleType,
            String keyword,
            Integer pageNum,
            Integer current,
            Integer pageSize
    );

    RoomStatusesRoomsResponseVO getRoomStatusesRooms(
            Long campId,
            Long userId,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            Long storeId,
            String queryCode,
            Integer page,
            Integer pageNum,
            Integer current,
            Integer pageSize
    );
}
