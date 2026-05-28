package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.StatementOrderQueryRowVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StatementOrderMapper {

    long countPage(
            @Param("campId") Long campId,
            @Param("poiIds") List<Long> poiIds,
            @Param("bookingStart") LocalDateTime bookingStart,
            @Param("bookingEndExclusive") LocalDateTime bookingEndExclusive,
            @Param("breakTemp") Boolean breakTemp
    );

    List<StatementOrderQueryRowVO> selectPage(
            @Param("campId") Long campId,
            @Param("poiIds") List<Long> poiIds,
            @Param("bookingStart") LocalDateTime bookingStart,
            @Param("bookingEndExclusive") LocalDateTime bookingEndExclusive,
            @Param("breakTemp") Boolean breakTemp,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );
}
