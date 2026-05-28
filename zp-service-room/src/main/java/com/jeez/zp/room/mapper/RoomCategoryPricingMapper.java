package com.jeez.zp.room.mapper;

import com.jeez.zp.room.vo.RoomCategoryPricingRowVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoomCategoryPricingMapper {

    List<RoomCategoryPricingRowVO> selectRows(
            @Param("campId") Long campId,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("channelIds") List<Long> channelIds
    );
}
