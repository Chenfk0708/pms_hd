package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.RoomSituationInventoryRowVO;
import com.jeez.zp.platform.vo.RoomSituationOrderRowVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomSituationReportMapper {

    List<RoomSituationInventoryRowVO> selectInventoryRows(
            @Param("campId") Long campId,
            @Param("poiIds") List<Long> poiIds
    );

    List<RoomSituationOrderRowVO> selectOrderRows(
            @Param("campId") Long campId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEndExclusive") LocalDateTime rangeEndExclusive,
            @Param("poiIds") List<Long> poiIds
    );
}
