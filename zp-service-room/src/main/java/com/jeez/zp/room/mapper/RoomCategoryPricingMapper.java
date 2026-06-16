package com.jeez.zp.room.mapper;

import com.jeez.zp.room.vo.RoomCategoryPricingRowVO;
import com.jeez.zp.room.vo.ChannelProductCoefficientRowVO;
import com.jeez.zp.room.vo.RoomCategoryAvailableStockRowVO;
import com.jeez.zp.room.vo.RoomCategoryPriceSnapshotRowVO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
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

    List<RoomCategoryPriceSnapshotRowVO> selectChannelPriceSnapshots(
            @Param("campId") Long campId,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("channelIds") List<Long> channelIds,
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

    List<ChannelProductCoefficientRowVO> selectProductCoefficients(
            @Param("campId") Long campId,
            @Param("roomCategoryIds") List<Long> roomCategoryIds,
            @Param("channelIds") List<Long> channelIds
    );

    int upsertProductCoefficient(
            @Param("id") Long id,
            @Param("campId") Long campId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("channelId") Long channelId,
            @Param("productName") String productName,
            @Param("operator") String operator,
            @Param("coefficientValue") BigDecimal coefficientValue
    );

    int upsertCentralSaleStatus(
            @Param("id") Long id,
            @Param("campId") Long campId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("bizDate") LocalDate bizDate,
            @Param("snapshotStatus") String snapshotStatus
    );

    int saveChannelPriceSnapshot(
            @Param("id") Long id,
            @Param("campId") Long campId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("channelId") Long channelId,
            @Param("bizDate") LocalDate bizDate,
            @Param("priceCent") Long priceCent,
            @Param("overwriteStandalone") boolean overwriteStandalone
    );
}
