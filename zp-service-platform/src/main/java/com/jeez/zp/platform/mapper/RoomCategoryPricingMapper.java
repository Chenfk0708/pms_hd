package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.RoomCategoryPricingRowVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoomCategoryPricingMapper {

    List<RoomCategoryPricingRowVO> selectRows(
            @Param("campId") Long campId,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("channelIds") List<Long> channelIds
    );
}
