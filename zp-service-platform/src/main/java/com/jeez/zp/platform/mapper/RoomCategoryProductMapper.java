package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.ChannelRoomCategoryPageRowVO;
import com.jeez.zp.platform.vo.ChannelRoomCategoryProductVO;
import com.jeez.zp.platform.vo.RoomCategoryProductPageItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoomCategoryProductMapper {

    long countPage(
            @Param("campId") Long campId,
            @Param("keyword") String keyword,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("channelId") Long channelId
    );

    List<RoomCategoryProductPageItemVO> selectPage(
            @Param("campId") Long campId,
            @Param("keyword") String keyword,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("channelId") Long channelId,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );

    long countChannelRoomCategoryPageV2(
            @Param("campId") Long campId,
            @Param("roomCategoryTypes") List<Integer> roomCategoryTypes,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("keyword") String keyword,
            @Param("channelIds") List<Long> channelIds,
            @Param("poiIds") List<Long> poiIds,
            @Param("shelfStatuses") List<String> shelfStatuses
    );

    List<ChannelRoomCategoryPageRowVO> selectChannelRoomCategoryPageV2(
            @Param("campId") Long campId,
            @Param("roomCategoryTypes") List<Integer> roomCategoryTypes,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("keyword") String keyword,
            @Param("channelIds") List<Long> channelIds,
            @Param("poiIds") List<Long> poiIds,
            @Param("shelfStatuses") List<String> shelfStatuses,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );

    List<ChannelRoomCategoryProductVO> selectChannelRoomCategoryProductRows(@Param("goodsIds") List<Long> goodsIds);

}
