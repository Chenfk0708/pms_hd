package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.DistributionFlowQueryRowVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DistributionFlowMapper {

    long countPage(
            @Param("campId") Long campId,
            @Param("bookingStart") LocalDateTime bookingStart,
            @Param("bookingEndExclusive") LocalDateTime bookingEndExclusive,
            @Param("keyword") String keyword,
            @Param("breakTemp") Boolean breakTemp,
            @Param("settledState") String settledState
    );

    List<DistributionFlowQueryRowVO> selectPage(
            @Param("campId") Long campId,
            @Param("bookingStart") LocalDateTime bookingStart,
            @Param("bookingEndExclusive") LocalDateTime bookingEndExclusive,
            @Param("keyword") String keyword,
            @Param("breakTemp") Boolean breakTemp,
            @Param("settledState") String settledState,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );

    String selectCampName(@Param("campId") Long campId);
}
