package com.jeez.zp.room.mapper;

import com.jeez.zp.room.vo.CleanTaskOptionVO;
import com.jeez.zp.room.vo.CleanTaskQueryRowVO;
import com.jeez.zp.room.vo.CleanTaskRoomRowVO;
import com.jeez.zp.room.vo.CleanerPageQueryRowVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    List<CleanerPageQueryRowVO> selectCleanerPageRows(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("keyword") String keyword,
            @Param("serviceDate") LocalDate serviceDate
    );

    CleanTaskRoomRowVO selectRoomForTask(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomId") Long roomId
    );

    int countCleaner(
            @Param("campId") Long campId,
            @Param("cleanStaffId") Long cleanStaffId
    );

    int insertCleanTask(
            @Param("cleanTaskId") Long cleanTaskId,
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomId") Long roomId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("cleanStaffId") Long cleanStaffId,
            @Param("taskType") String taskType,
            @Param("taskStatus") String taskStatus,
            @Param("deadlineAt") LocalDateTime deadlineAt,
            @Param("remark") String remark
    );

    List<String> selectTaskIds(
            @Param("campId") Long campId,
            @Param("taskIds") List<Long> taskIds
    );

    int incrementNotifyCount(
            @Param("campId") Long campId,
            @Param("taskIds") List<Long> taskIds
    );
}
