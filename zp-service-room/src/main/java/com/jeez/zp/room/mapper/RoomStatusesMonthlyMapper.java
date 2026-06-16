package com.jeez.zp.room.mapper;

import com.jeez.zp.room.vo.RoomStatusesMonthlyDailyMonitorVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyInventoryVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyOccVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyOrderDetailVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyBlockVO;
import com.jeez.zp.room.vo.RoomStatusCloseRoomMetaVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomStatusesMonthlyMapper {

    RoomStatusCloseRoomMetaVO selectCloseRoomMeta(
            @Param("campId") Long campId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("roomId") Long roomId
    );

    int countExistingClosedBlock(
            @Param("campId") Long campId,
            @Param("roomId") Long roomId,
            @Param("bizDate") LocalDate bizDate
    );

    int countOrdersOnRoomDate(
            @Param("campId") Long campId,
            @Param("roomId") Long roomId,
            @Param("bizDate") LocalDate bizDate
    );

    List<RoomStatusCloseRoomMetaVO> selectCentralSaleStatusCloseRoomMetas(
            @Param("campId") Long campId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("bizDate") LocalDate bizDate
    );

    List<RoomStatusCloseRoomMetaVO> selectCentralSaleStatusClosedBlockMetas(
            @Param("campId") Long campId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("bizDate") LocalDate bizDate,
            @Param("reason") String reason
    );

    int upsertClosedBlock(
            @Param("blockId") Long blockId,
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("bizDate") LocalDate bizDate,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("roomId") Long roomId,
            @Param("reason") String reason,
            @Param("userId") Long userId
    );

    int upsertCentralSaleStatusClosedBlock(
            @Param("blockId") Long blockId,
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("bizDate") LocalDate bizDate,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("roomId") Long roomId,
            @Param("reason") String reason,
            @Param("userId") Long userId
    );

    int updateDailyCountersForClose(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("bizDate") LocalDate bizDate,
            @Param("roomCategoryId") Long roomCategoryId
    );

    int insertDailyCountersForClose(
            @Param("id") Long id,
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("bizDate") LocalDate bizDate,
            @Param("roomCategoryId") Long roomCategoryId
    );

    int openClosedBlock(
            @Param("campId") Long campId,
            @Param("roomId") Long roomId,
            @Param("bizDate") LocalDate bizDate,
            @Param("userId") Long userId
    );

    int openCentralSaleStatusClosedBlock(
            @Param("campId") Long campId,
            @Param("roomId") Long roomId,
            @Param("bizDate") LocalDate bizDate,
            @Param("reason") String reason,
            @Param("userId") Long userId
    );

    int updateDailyCountersForOpen(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("bizDate") LocalDate bizDate,
            @Param("roomCategoryId") Long roomCategoryId
    );

    int updateRoomCleanStatus(
            @Param("campId") Long campId,
            @Param("roomId") Long roomId,
            @Param("cleanStatus") String cleanStatus,
            @Param("userId") Long userId
    );

    List<RoomStatusesMonthlyBlockVO> selectBlockRows(
            @Param("campId") Long campId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("poiIds") List<Long> poiIds,
            @Param("keyword") String keyword
    );

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
