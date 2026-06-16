package com.jeez.zp.room.mapper;

import com.jeez.zp.room.vo.CleanLogRowVO;
import com.jeez.zp.room.vo.CleanLogOptionVO;
import com.jeez.zp.room.vo.CleanLogRoomOptionVO;
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

    List<CleanLogOptionVO> selectStores(@Param("campId") Long campId);

    List<CleanLogRoomOptionVO> selectRooms(@Param("campId") Long campId);

    List<CleanLogOptionVO> selectOperators(@Param("campId") Long campId);
}
