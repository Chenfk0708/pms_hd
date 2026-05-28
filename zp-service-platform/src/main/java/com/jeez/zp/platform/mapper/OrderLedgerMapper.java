package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.OrderLedgerOptionVO;
import com.jeez.zp.platform.vo.OrderLedgerQueryRowVO;
import com.jeez.zp.platform.vo.OrderLedgerStoreOptionVO;
import com.jeez.zp.platform.vo.OrderLedgerSummaryQueryVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderLedgerMapper {

    List<OrderLedgerStoreOptionVO> selectStores(@Param("campId") Long campId);

    List<OrderLedgerOptionVO> selectProjectOptions(@Param("campId") Long campId, @Param("isIncome") Integer isIncome);

    List<OrderLedgerOptionVO> selectPaymentWayOptions(@Param("campId") Long campId);

    long countPage(
            @Param("campId") Long campId,
            @Param("beginTime") LocalDateTime beginTime,
            @Param("endTimeExclusive") LocalDateTime endTimeExclusive,
            @Param("paymentTypeIds") List<Long> paymentTypeIds,
            @Param("paymentWayIds") List<Long> paymentWayIds,
            @Param("roomIds") List<Long> roomIds,
            @Param("poiIds") List<Long> poiIds,
            @Param("keyword") String keyword,
            @Param("isIncome") Integer isIncome,
            @Param("type") Integer type
    );

    OrderLedgerSummaryQueryVO selectSummary(
            @Param("campId") Long campId,
            @Param("beginTime") LocalDateTime beginTime,
            @Param("endTimeExclusive") LocalDateTime endTimeExclusive,
            @Param("paymentTypeIds") List<Long> paymentTypeIds,
            @Param("paymentWayIds") List<Long> paymentWayIds,
            @Param("roomIds") List<Long> roomIds,
            @Param("poiIds") List<Long> poiIds,
            @Param("keyword") String keyword,
            @Param("isIncome") Integer isIncome,
            @Param("type") Integer type
    );

    List<OrderLedgerQueryRowVO> selectPage(
            @Param("campId") Long campId,
            @Param("beginTime") LocalDateTime beginTime,
            @Param("endTimeExclusive") LocalDateTime endTimeExclusive,
            @Param("paymentTypeIds") List<Long> paymentTypeIds,
            @Param("paymentWayIds") List<Long> paymentWayIds,
            @Param("roomIds") List<Long> roomIds,
            @Param("poiIds") List<Long> poiIds,
            @Param("keyword") String keyword,
            @Param("isIncome") Integer isIncome,
            @Param("type") Integer type,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );
}
