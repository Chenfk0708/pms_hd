package com.jeez.zp.room.mapper;

import com.jeez.zp.room.vo.RoomStatusesMonthlyDailyMonitorVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyInventoryVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyOccVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyOrderDetailVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomStatusesMonthlyMapper {

    List<RoomStatusesMonthlyInventoryVO> selectInventoryRows(
            @Param("campId") Long campId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("poiIds") List<Long> poiIds,
            @Param("keyword") String keyword
    );

    List<RoomStatusesMonthlyDailyMonitorVO> selectDailyMonitorRows(
            @Param("campId") Long campId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("poiIds") List<Long> poiIds,
            @Param("keyword") String keyword
    );

    List<RoomStatusesMonthlyOccVO> selectOccRows(
            @Param("campId") Long campId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("poiIds") List<Long> poiIds,
            @Param("keyword") String keyword
    );

    long countOrderDetails(
            @Param("campId") Long campId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("poiIds") List<Long> poiIds,
            @Param("keyword") String keyword
    );

    List<RoomStatusesMonthlyOrderDetailVO> selectOrderDetails(
            @Param("campId") Long campId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("poiIds") List<Long> poiIds,
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );
}