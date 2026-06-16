package com.jeez.zp.order.mapper;

import com.jeez.zp.order.vo.OrderActionRowVO;
import com.jeez.zp.order.vo.OrderChangeRoomOptionVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    int countActiveRoomBySelection(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("roomId") Long roomId
    );

    int countOverlappingActiveOrders(
            @Param("campId") Long campId,
            @Param("roomId") Long roomId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("excludeOrderId") Long excludeOrderId
    );

    int countOverlappingClosedRoomBlocks(
            @Param("campId") Long campId,
            @Param("roomId") Long roomId,
            @Param("blockStartDate") LocalDate blockStartDate,
            @Param("blockEndDate") LocalDate blockEndDate
    );

    OrderActionRowVO selectOrderForUpdate(@Param("campId") Long campId, @Param("orderId") Long orderId);

    List<OrderChangeRoomOptionVO> selectChangeRoomOptions(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("currentRoomId") Long currentRoomId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("blockStartDate") LocalDate blockStartDate,
            @Param("blockEndDate") LocalDate blockEndDate,
            @Param("excludeOrderId") Long excludeOrderId
    );

    OrderChangeRoomOptionVO selectRoomForChangeRoom(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("roomId") Long roomId
    );

    OrderActionRowVO selectOrderTimes(@Param("orderId") Long orderId);

    int updateOrderRoom(
            @Param("campId") Long campId,
            @Param("orderId") Long orderId,
            @Param("roomId") Long roomId,
            @Param("roomNameSnapshot") String roomNameSnapshot,
            @Param("roomSnapshotJson") String roomSnapshotJson,
            @Param("remark") String remark,
            @Param("userId") Long userId
    );

    int releaseOrderInventoryAndArrangement(
            @Param("campId") Long campId,
            @Param("orderId") Long orderId,
            @Param("roomSnapshotJson") String roomSnapshotJson,
            @Param("remark") String remark,
            @Param("userId") Long userId
    );

    int updateOrderStatus(
            @Param("campId") Long campId,
            @Param("orderId") Long orderId,
            @Param("status") String status,
            @Param("paymentStatus") String paymentStatus,
            @Param("remark") String remark,
            @Param("guestRegisteredAt") LocalDateTime guestRegisteredAt,
            @Param("checkedOutAt") LocalDateTime checkedOutAt,
            @Param("userId") Long userId
    );

    int updateRoomCleanStatus(
            @Param("campId") Long campId,
            @Param("roomId") Long roomId,
            @Param("cleanStatus") String cleanStatus
    );

    int countOpenCheckoutCleanTasks(
            @Param("campId") Long campId,
            @Param("roomId") Long roomId
    );

    int insertCheckoutCleanTask(
            @Param("cleanTaskId") Long cleanTaskId,
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomId") Long roomId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("deadlineAt") LocalDateTime deadlineAt,
            @Param("remark") String remark
    );

    int insertCleanLog(
            @Param("cleanLogId") Long cleanLogId,
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("roomId") Long roomId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("cleanTaskId") Long cleanTaskId,
            @Param("cleanStaffId") Long cleanStaffId,
            @Param("operatorId") Long operatorId,
            @Param("actionType") String actionType,
            @Param("actionDetail") String actionDetail
    );

    int updateGuestRegisteredAt(
            @Param("campId") Long campId,
            @Param("orderId") Long orderId,
            @Param("guestRegisteredAt") LocalDateTime guestRegisteredAt,
            @Param("userId") Long userId
    );

    int countOrderGuests(@Param("orderId") Long orderId);

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
