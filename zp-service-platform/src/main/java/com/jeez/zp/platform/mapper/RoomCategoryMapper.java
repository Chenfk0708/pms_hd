package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.RoomCategoryPageItemVO;
import com.jeez.zp.platform.vo.RoomCategoryDetailChannelPriceRowVO;
import com.jeez.zp.platform.vo.RoomCategoryDetailRowVO;
import com.jeez.zp.platform.vo.RoomCategoryRoomViewVO;
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

    RoomCategoryDetailRowVO selectDetail(@Param("campId") Long campId, @Param("roomCategoryId") Long roomCategoryId);

    List<RoomCategoryDetailChannelPriceRowVO> selectDetailChannelPriceRows(
            @Param("campId") Long campId,
            @Param("roomCategoryId") Long roomCategoryId
    );
}
