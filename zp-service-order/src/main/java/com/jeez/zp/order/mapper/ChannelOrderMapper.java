package com.jeez.zp.order.mapper;

import com.jeez.zp.order.vo.ChannelOrderAccountVO;
import com.jeez.zp.order.vo.ChannelOrderAvailableRoomVO;
import com.jeez.zp.order.vo.ChannelOrderMappingVO;
import com.jeez.zp.order.vo.ChannelOrderRawVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ChannelOrderMapper {

    ChannelOrderAccountVO selectAccount(@Param("accountId") Long accountId);

    ChannelOrderRawVO selectRawByExternalOrder(
            @Param("accountId") Long accountId,
            @Param("outOrderNo") String outOrderNo
    );

    ChannelOrderMappingVO selectMapping(
            @Param("accountId") Long accountId,
            @Param("outPoiId") String outPoiId,
            @Param("outRoomCategoryId") String outRoomCategoryId
    );

    List<ChannelOrderMappingVO> selectMappingsByRoomCategoryName(
            @Param("accountId") Long accountId,
            @Param("outPoiId") String outPoiId,
            @Param("roomCategoryName") String roomCategoryName
    );

    Integer countAvailableRoomCategoryStock(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("blockStartDate") LocalDate blockStartDate,
            @Param("blockEndDate") LocalDate blockEndDate,
            @Param("excludeOrderId") Long excludeOrderId
    );

    ChannelOrderAvailableRoomVO selectAvailableRoomForAutoArrange(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("blockStartDate") LocalDate blockStartDate,
            @Param("blockEndDate") LocalDate blockEndDate,
            @Param("excludeOrderId") Long excludeOrderId
    );

    int upsertRawProcessing(
            @Param("rawId") Long rawId,
            @Param("campId") Long campId,
            @Param("accountId") Long accountId,
            @Param("channelId") Long channelId,
            @Param("channelCode") String channelCode,
            @Param("outOrderNo") String outOrderNo,
            @Param("rawPayloadJson") String rawPayloadJson
    );

    int updateRawSuccess(
            @Param("accountId") Long accountId,
            @Param("outOrderNo") String outOrderNo,
            @Param("pmsOrderId") Long pmsOrderId,
            @Param("rawPayloadJson") String rawPayloadJson
    );

    int updateRawFailure(
            @Param("accountId") Long accountId,
            @Param("outOrderNo") String outOrderNo,
            @Param("rawPayloadJson") String rawPayloadJson,
            @Param("errorMessage") String errorMessage
    );

    int insertOrderMain(
            @Param("orderId") Long orderId,
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("roomId") Long roomId,
            @Param("channelAccountId") Long channelAccountId,
            @Param("orderNo") String orderNo,
            @Param("internalOutOrderNo") String internalOutOrderNo,
            @Param("externalOutOrderNo") String externalOutOrderNo,
            @Param("guestName") String guestName,
            @Param("guestMobile") String guestMobile,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("dayNum") Integer dayNum,
            @Param("totalPriceCent") Long totalPriceCent,
            @Param("totalPayPriceCent") Long totalPayPriceCent,
            @Param("commissionPriceCent") Long commissionPriceCent,
            @Param("paymentStatus") String paymentStatus,
            @Param("sourceLabelSnapshot") String sourceLabelSnapshot,
            @Param("poiNameSnapshot") String poiNameSnapshot,
            @Param("roomCategoryNameSnapshot") String roomCategoryNameSnapshot,
            @Param("roomNameSnapshot") String roomNameSnapshot,
            @Param("roomSnapshotJson") String roomSnapshotJson,
            @Param("remark") String remark,
            @Param("userId") Long userId
    );

    int insertDistributionOrder(
            @Param("distributionOrderId") Long distributionOrderId,
            @Param("campId") Long campId,
            @Param("orderId") Long orderId,
            @Param("channelAccountId") Long channelAccountId,
            @Param("commissionPriceCent") Long commissionPriceCent
    );
}
