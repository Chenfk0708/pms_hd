package com.jeez.zp.room.mapper;

import com.jeez.zp.room.vo.CleanTaskOptionVO;
import com.jeez.zp.room.vo.CleanTaskQueryRowVO;
import org.apache.ibatis.annotations.Param;

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

    List<CleanTaskOptionVO> selectStores(@Param("campId") Long campId);

    List<CleanTaskOptionVO> selectRooms(@Param("campId") Long campId);

    List<CleanTaskOptionVO> selectCleaners(@Param("campId") Long campId);
}
