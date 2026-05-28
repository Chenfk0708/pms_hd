package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.ComprehensiveMonthlyReportQueryRowVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ComprehensiveMonthlyReportMapper {

    List<ComprehensiveMonthlyReportQueryRowVO> selectOrderRows(
            @Param("campId") Long campId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEndExclusive") LocalDateTime rangeEndExclusive
    );

    Integer countActiveRooms(@Param("campId") Long campId);

    Integer sumRoomCategoryCount(@Param("campId") Long campId);
}
