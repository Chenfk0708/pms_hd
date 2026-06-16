package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.SalesReportOrderRowVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesReportMapper {

    List<SalesReportOrderRowVO> selectOrderRows(
            @Param("campId") Long campId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEndExclusive") LocalDateTime rangeEndExclusive,
            @Param("poiIds") List<Long> poiIds,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("roomCategoryGroupIds") List<Long> roomCategoryGroupIds,
            @Param("channelIds") List<Long> channelIds,
            @Param("roomIds") List<Long> roomIds
    );

    Integer countActiveRooms(@Param("campId") Long campId);
}
