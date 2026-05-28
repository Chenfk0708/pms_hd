package com.jeez.zp.room.mapper;

import com.jeez.zp.room.vo.CleanSettingOptionVO;
import com.jeez.zp.room.vo.CleanSettingRowVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface CleanSettingMapper {

    List<CleanSettingOptionVO> selectStores(@Param("campId") Long campId);

    List<CleanSettingRowVO> selectSettings(@Param("campId") Long campId);

    long countActiveTasks(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("businessDate") LocalDate businessDate
    );

    long countPendingTasks(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("businessDate") LocalDate businessDate
    );

    long countActiveTasksByHourRange(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("businessDate") LocalDate businessDate,
            @Param("startHour") int startHour,
            @Param("endHour") int endHour
    );
}
