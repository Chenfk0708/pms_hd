package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.RoomStatusesTodayRowVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomStatusesTodayMapper {

    List<RoomStatusesTodayRowVO> selectRows(
            @Param("campId") Long campId,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("nextDayStart") LocalDateTime nextDayStart,
            @Param("roomCategoryGroupIds") List<Long> roomCategoryGroupIds,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("poiIds") List<Long> poiIds
    );
}
