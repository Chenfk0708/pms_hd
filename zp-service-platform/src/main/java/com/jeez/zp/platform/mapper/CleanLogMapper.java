package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.CleanLogRowVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CleanLogMapper {

    List<CleanLogRowVO> selectRows(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomIds") List<Long> roomIds,
            @Param("operatorId") Long operatorId,
            @Param("operatorStartTime") LocalDateTime operatorStartTime,
            @Param("operatorEndExclusiveTime") LocalDateTime operatorEndExclusiveTime
    );
}
