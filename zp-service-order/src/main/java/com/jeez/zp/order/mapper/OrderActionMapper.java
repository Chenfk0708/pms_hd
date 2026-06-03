package com.jeez.zp.order.mapper;

import com.jeez.zp.order.vo.OrderActionRowVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface OrderActionMapper {

    int insertOrderMain(
            @Param("orderId") Long orderId,
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("roomId") Long roomId,
            @Param("orderType") String orderType,
            @Param("orderNo") String orderNo,
            @Param("outOrderNo") String outOrderNo,
            @Param("guestName") String guestName,
            @Param("guestMobile") String guestMobile,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("dayNum") Integer dayNum,
            @Param("totalPriceCent") Long totalPriceCent,
            @Param("totalPayPriceCent") Long totalPayPriceCent,
            @Param("commissionPriceCent") Long commissionPriceCent,
            @Param("depositPriceCent") Long depositPriceCent,
            @Param("otherPriceCent") Long otherPriceCent,
            @Param("paymentStatus") String paymentStatus,
            @Param("paymentTypeId") Long paymentTypeId,
            @Param("paymentWayId") Long paymentWayId,
            @Param("sourceType") String sourceType,
            @Param("poiNameSnapshot") String poiNameSnapshot,
            @Param("roomCategoryNameSnapshot") String roomCategoryNameSnapshot,
            @Param("roomNameSnapshot") String roomNameSnapshot,
            @Param("stayTypeSnapshot") String stayTypeSnapshot,
            @Param("sourceLabelSnapshot") String sourceLabelSnapshot,
            @Param("channelOrderNoSnapshot") String channelOrderNoSnapshot,
            @Param("invoiceIssuer") String invoiceIssuer,
            @Param("invoiceAmountCent") Long invoiceAmountCent,
            @Param("emergencyName") String emergencyName,
            @Param("emergencyMobile") String emergencyMobile,
            @Param("paymentCycle") String paymentCycle,
            @Param("paymentMonth") String paymentMonth,
            @Param("paymentDay") String paymentDay,
            @Param("roomChargeStatus") String roomChargeStatus,
            @Param("roomChargeReceivedCent") Long roomChargeReceivedCent,
            @Param("roomChargeMethod") String roomChargeMethod,
            @Param("depositChargeStatus") String depositChargeStatus,
            @Param("depositChargeReceivedCent") Long depositChargeReceivedCent,
            @Param("depositChargeMethod") String depositChargeMethod,
            @Param("reminderEnabled") Integer reminderEnabled,
            @Param("contractDueMode") String contractDueMode,
            @Param("contractNo") String contractNo,
            @Param("nextPaymentDate") LocalDate nextPaymentDate,
            @Param("nextPaymentAmountCent") Long nextPaymentAmountCent,
            @Param("extraFeeCent") Long extraFeeCent,
            @Param("roomSnapshotJson") String roomSnapshotJson,
            @Param("orderTagsJson") String orderTagsJson,
            @Param("orderRemindersJson") String orderRemindersJson,
            @Param("extraFeeItemsJson") String extraFeeItemsJson,
            @Param("billingSnapshotJson") String billingSnapshotJson,
            @Param("remark") String remark,
            @Param("userId") Long userId
    );

    OrderActionRowVO selectOrderForUpdate(@Param("campId") Long campId, @Param("orderId") Long orderId);

    int updateOrderStatus(
            @Param("campId") Long campId,
            @Param("orderId") Long orderId,
            @Param("status") String status,
            @Param("paymentStatus") String paymentStatus,
            @Param("remark") String remark,
            @Param("userId") Long userId
    );

    int deleteOrderGuests(@Param("orderId") Long orderId);

    int insertOrderGuest(
            @Param("guestId") Long guestId,
            @Param("orderId") Long orderId,
            @Param("guestName") String guestName,
            @Param("guestMobile") String guestMobile,
            @Param("guestIdCardType") String guestIdCardType,
            @Param("guestIdCard") String guestIdCard,
            @Param("guestType") String guestType
    );
}
