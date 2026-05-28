package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.RoomCategoryRoomsGroupVO;
import com.jeez.zp.platform.vo.RoomItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoomMapper {

    List<RoomCategoryRoomsGroupVO> selectRoomCategoryGroups(
            @Param("campId") Long campId,
            @Param("roomCategoryIds") List<Long> roomCategoryIds
    );

    List<RoomItemVO> selectRooms(
            @Param("campId") Long campId,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("saleType") Integer saleType
    );
}
