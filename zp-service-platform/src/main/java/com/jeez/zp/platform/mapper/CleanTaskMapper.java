package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.CleanTaskOptionVO;
import com.jeez.zp.platform.vo.CleanTaskQueryRowVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

public interface CleanTaskMapper {

    List<CleanTaskQueryRowVO> selectTaskRows(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomId") Long roomId,
            @Param("cleanerIds") List<Long> cleanerIds,
            @Param("cleanDate") LocalDate cleanDate
    );

    List<CleanTaskQueryRowVO> selectTaskRowsByRange(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomIds") List<Long> roomIds,
            @Param("cleanerIds") List<Long> cleanerIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<CleanTaskOptionVO> selectStores(@Param("campId") Long campId);

    List<CleanTaskOptionVO> selectRooms(@Param("campId") Long campId);

    List<CleanTaskOptionVO> selectCleaners(@Param("campId") Long campId);
}
