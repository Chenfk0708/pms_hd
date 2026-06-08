package com.jeez.zp.room.mapper;

import com.jeez.zp.room.vo.RoomCategoryPricingRowVO;
import com.jeez.zp.room.vo.RoomCategoryAvailableStockRowVO;
import com.jeez.zp.room.vo.RoomCategoryPriceSnapshotRowVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomCategoryPricingMapper {

    List<RoomCategoryPricingRowVO> selectRows(
            @Param("campId") Long campId,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("channelIds") List<Long> channelIds,
            @Param("poiIds") List<Long> poiIds
    );

    List<RoomCategoryPricingRowVO> selectRetailRows(
            @Param("campId") Long campId,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("poiIds") List<Long> poiIds
    );

    List<RoomCategoryPriceSnapshotRowVO> selectCentralPriceSnapshots(
            @Param("campId") Long campId,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<RoomCategoryAvailableStockRowVO> selectAvailableStockRows(
            @Param("campId") Long campId,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("poiIds") List<Long> poiIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    int upsertCentralSaleStatus(
            @Param("id") Long id,
            @Param("campId") Long campId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("bizDate") LocalDate bizDate,
            @Param("snapshotStatus") String snapshotStatus
    );
}
