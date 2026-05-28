package com.jeez.zp.room.mapper;

import com.jeez.zp.room.vo.RoomCategoryPageItemVO;
import com.jeez.zp.room.vo.RoomCategoryRoomViewVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoomCategoryMapper {

    long countPage(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomCategoryGroupId") Long roomCategoryGroupId,
            @Param("roomCategoryName") String roomCategoryName,
            @Param("keyword") String keyword,
            @Param("channelId") Long channelId
    );

    List<RoomCategoryPageItemVO> selectPage(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomCategoryGroupId") Long roomCategoryGroupId,
            @Param("roomCategoryName") String roomCategoryName,
            @Param("keyword") String keyword,
            @Param("channelId") Long channelId,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );

    List<RoomCategoryRoomViewVO> selectRoomViews(@Param("roomCategoryIds") List<Long> roomCategoryIds);
}
