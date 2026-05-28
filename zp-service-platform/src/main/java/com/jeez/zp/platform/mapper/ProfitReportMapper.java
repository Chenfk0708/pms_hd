package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.ProfitReportCleanTaskRowVO;
import com.jeez.zp.platform.vo.ProfitReportOrderRowVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProfitReportMapper {

    List<ProfitReportOrderRowVO> selectOrderRows(
            @Param("campId") Long campId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEndExclusive") LocalDateTime rangeEndExclusive,
            @Param("poiId") Long poiId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("roomCategoryGroupId") Long roomCategoryGroupId,
            @Param("channelId") Long channelId,
            @Param("roomId") Long roomId
    );

    List<ProfitReportCleanTaskRowVO> selectCleanTaskRows(
            @Param("campId") Long campId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEndExclusive") LocalDateTime rangeEndExclusive,
            @Param("poiId") Long poiId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("roomCategoryGroupId") Long roomCategoryGroupId,
            @Param("roomId") Long roomId
    );
}
