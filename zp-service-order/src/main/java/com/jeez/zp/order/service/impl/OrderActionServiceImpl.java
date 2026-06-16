package com.jeez.zp.order.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.jeez.common.utils.InputValidationUtils;
import com.jeez.zp.order.dto.request.OrderCreateRequest;
import com.jeez.zp.order.dto.request.OrderCreateRoomItemRequest;
import com.jeez.zp.order.dto.request.OrderGuestSaveItemRequest;
import com.jeez.zp.order.dto.request.OrderGuestsSaveRequest;
import com.jeez.zp.order.exception.BusinessException;
import com.jeez.zp.order.mapper.OrderActionMapper;
import com.jeez.zp.order.mapper.UserCampMapper;
import com.jeez.zp.order.service.OrderActionService;
import com.jeez.zp.order.service.OrderSensitiveDataCipher;
import com.jeez.zp.order.vo.OrderActionResponseVO;
import com.jeez.zp.order.vo.OrderActionRowVO;
import com.jeez.zp.order.vo.OrderChangeRoomOptionVO;
import com.jeez.zp.order.vo.OrderChangeRoomOptionsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderActionServiceImpl implements OrderActionService {

    private static final String STATUS_BOOKED = "booked";
    private static final String STATUS_CHECKED_IN = "checked_in";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_CANCELLED = "cancelled";
    private static final String STATUS_NO_SHOW = "no_show";
    private static final String ROOM_CLEAN_STATUS_DIRTY = "dirty";
    private static final String CLEAN_LOG_ACTION_CHECKOUT_AUTO_CREATE = "checkout_auto_create";
    private static final String DEFAULT_LOCAL_CHANNEL_NAME = "宿银平台";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderActionMapper orderActionMapper;
    private final UserCampMapper userCampMapper;
    private final OrderSensitiveDataCipher sensitiveDataCipher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public OrderActionResponseVO createOrder(OrderCreateRequest request, Long userId) {
        Long campId = resolveAccessibleCampId(parseRequiredLong(request.getCampId(), "campId"), userId);
        Long orderId = parseLong(request.getOrderId());
        if (orderId == null) {
            orderId = generateOrderId();
        }
        Long poiId = parseRequiredLong(request.getPoiId(), "poiId");
        Long roomCategoryId = parseLong(request.getRoomCategoryId());
        Long roomId = parseLong(request.getRoomId());
        LocalDateTime startAt = parseRequiredDateTime(request.getCheckInDate(), "checkInDate", LocalTime.of(14, 0));
        LocalDateTime endAt = parseRequiredDateTime(request.getCheckOutDate(), "checkOutDate", LocalTime.of(12, 0));
        if (!endAt.isAfter(startAt)) {
            throw new BusinessException(40001, "checkOutDate must be after checkInDate");
        }

        String orderType = normalizeOrderType(request.getOrderType(), request.getStayType());
        validateRoomSelection(campId, poiId, roomCategoryId, roomId, startAt, endAt, orderId, orderType);
        List<OrderCreateRoomItemRequest> normalizedRooms = normalizeRoomSnapshots(request.getRooms(), roomCategoryId, roomId);
        String sourceLabel = defaultString(request.getSourceLabel(), DEFAULT_LOCAL_CHANNEL_NAME);
        String guestName = normalizePersonName(request.getGuestName());
        String guestMobile = normalizeOptionalMainlandMobile(request.getGuestMobile());
        List<OrderGuestSaveItemRequest> normalizedGuests = normalizeGuests(request.getGuests());

        orderActionMapper.insertOrderMain(
                orderId,
                campId,
                poiId,
                roomCategoryId,
                roomId,
                orderType,
                "ORDER-ACTION-" + orderId,
                defaultString(request.getChannelOrderNo(), "OUT-ACTION-" + orderId),
                guestName,
                guestMobile,
                startAt,
                endAt,
                resolveDayNum(orderType, startAt, endAt),
                defaultLong(request.getTotalPrice()),
                defaultLong(request.getTotalPayPrice()),
                resolveCommissionPrice(request),
                defaultLong(request.getDepositPrice()),
                resolveOtherPrice(request),
                defaultString(request.getPaymentStatus(), "unpaid"),
                17101L,
                resolvePaymentWayId(request.getRoomChargeMethod()),
                resolveSourceType(sourceLabel),
                request.getPoiName(),
                request.getRoomCategoryName(),
                request.getRoomName(),
                request.getStayType(),
                sourceLabel,
                request.getChannelOrderNo(),
                request.getInvoiceIssuer(),
                defaultLong(request.getInvoiceAmount()),
                request.getEmergencyName(),
                request.getEmergencyMobile(),
                request.getPaymentCycle(),
                request.getPaymentMonth(),
                request.getPaymentDay(),
                request.getRoomChargeStatus(),
                defaultLong(request.getRoomChargeReceived()),
                request.getRoomChargeMethod(),
                request.getDepositChargeStatus(),
                defaultLong(request.getDepositChargeReceived()),
                request.getDepositChargeMethod(),
                request.getReminderEnabled() == null ? 0 : request.getReminderEnabled(),
                request.getContractDueMode(),
                request.getContractNo(),
                parseOptionalDate(request.getNextPaymentDate()),
                defaultLong(request.getNextPaymentAmount()),
                defaultLong(request.getExtraFee()),
                toJson(normalizedRooms),
                toJson(request.getTags()),
                toJson(request.getReminders()),
                toJson(request.getExtraFeeItems()),
                defaultJson(request.getBillingSnapshot()),
                request.getRemark(),
                userId
        );
        replaceGuests(orderId, normalizedGuests);
        return response(orderId, STATUS_BOOKED, normalizedGuests.size(), "订单创建成功");
    }

    @Override
    @Transactional
    public OrderActionResponseVO cancelOrder(Long campId, Long orderId, Long userId, String reason) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        OrderActionRowVO row = requireOrder(resolvedCampId, orderId);
        if (STATUS_COMPLETED.equals(row.getStatus())) {
            throw new BusinessException(40001, "completed order cannot be cancelled");
        }
        updateStatus(resolvedCampId, orderId, STATUS_CANCELLED, "cancelled", reason, null, null, userId);
        return response(orderId, STATUS_CANCELLED, null, "订单取消成功");
    }

    @Override
    @Transactional
    public OrderActionResponseVO skipStock(Long campId, Long orderId, Long userId, String reason) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        OrderActionRowVO row = requireOrder(resolvedCampId, orderId);
        if (!STATUS_BOOKED.equals(row.getStatus()) && !STATUS_CHECKED_IN.equals(row.getStatus())) {
            throw new BusinessException(40001, "只有预订中或入住中的订单可以设置不占库存");
        }

        String roomSnapshotJson = buildReleasedRoomSnapshotJson(row);
        String remark = appendSkipStockRemark(row.getRemark(), reason);
        int updated = orderActionMapper.releaseOrderInventoryAndArrangement(
                resolvedCampId,
                orderId,
                roomSnapshotJson,
                remark,
                userId
        );
        if (updated != 1) {
            throw new BusinessException(40401, "订单不存在");
        }

        OrderActionResponseVO response = response(orderId, row.getStatus(), null, "订单已释放库存并取消排房");
        response.setRoomId("");
        response.setRoomName("");
        response.setRoomCategoryId(stringValue(row.getRoomCategoryId()));
        response.setRoomCategoryName(row.getRoomCategoryName());
        return response;
    }

    @Override
    @Transactional
    public OrderActionResponseVO markNoShow(Long campId, Long orderId, Long userId, String reason) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        OrderActionRowVO row = requireOrder(resolvedCampId, orderId);
        if (!STATUS_BOOKED.equals(row.getStatus())) {
            throw new BusinessException(40001, "只有待入住订单可以标记未到店");
        }
        if (row.getStartAt() == null || LocalDateTime.now().isBefore(row.getStartAt())) {
            throw new BusinessException(40001, "未到入住时间，不能标记未到店");
        }

        String remark = appendNoShowRemark(row.getRemark(), reason);
        updateStatus(resolvedCampId, orderId, STATUS_NO_SHOW, null, remark, null, null, userId);
        return response(orderId, STATUS_NO_SHOW, null, "已标记为未到店");
    }

    @Override
    @Transactional
    public OrderActionResponseVO checkIn(Long campId, Long orderId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        OrderActionRowVO row = requireOrder(resolvedCampId, orderId);
        if (!STATUS_BOOKED.equals(row.getStatus())) {
            throw new BusinessException(40001, "only booked order can check in");
        }
        if (row.getGuestRegisteredAt() == null || orderActionMapper.countOrderGuests(orderId) <= 0) {
            throw new BusinessException(40001, "请先登记入住人");
        }
        updateStatus(resolvedCampId, orderId, STATUS_CHECKED_IN, null, null, null, null, userId);
        return response(orderId, STATUS_CHECKED_IN, null, "办理入住成功");
    }

    @Override
    @Transactional
    public OrderActionResponseVO checkOut(Long campId, Long orderId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        OrderActionRowVO row = requireOrder(resolvedCampId, orderId);
        if (!STATUS_CHECKED_IN.equals(row.getStatus())) {
            throw new BusinessException(40001, "only checked-in order can check out");
        }
        LocalDateTime checkedOutAt = LocalDateTime.now();
        updateStatus(resolvedCampId, orderId, STATUS_COMPLETED, "paid", null, null, checkedOutAt, userId);
        createCheckoutCleanTaskIfNeeded(row, checkedOutAt, userId);
        return response(orderId, STATUS_COMPLETED, null, "办理退房成功");
    }

    private void createCheckoutCleanTaskIfNeeded(OrderActionRowVO row, LocalDateTime checkedOutAt, Long userId) {
        if (row.getRoomId() == null || row.getRoomCategoryId() == null || row.getPoiId() == null) {
            return;
        }
        orderActionMapper.updateRoomCleanStatus(row.getCampId(), row.getRoomId(), ROOM_CLEAN_STATUS_DIRTY);
        if (orderActionMapper.countOpenCheckoutCleanTasks(row.getCampId(), row.getRoomId()) > 0) {
            return;
        }

        Long cleanTaskId = IdWorker.getId();
        LocalDateTime deadlineAt = checkedOutAt.plusHours(2);
        String detail = "退房自动派单：订单 " + row.getOrderId();
        orderActionMapper.insertCheckoutCleanTask(
                cleanTaskId,
                row.getCampId(),
                row.getPoiId(),
                row.getRoomId(),
                row.getRoomCategoryId(),
                deadlineAt,
                detail
        );
        orderActionMapper.insertCleanLog(
                IdWorker.getId(),
                row.getCampId(),
                row.getPoiId(),
                row.getRoomId(),
                row.getRoomCategoryId(),
                cleanTaskId,
                null,
                userId,
                CLEAN_LOG_ACTION_CHECKOUT_AUTO_CREATE,
                detail
        );
    }

    @Override
    @Transactional
    public OrderActionResponseVO saveGuests(Long campId, Long orderId, Long userId, OrderGuestsSaveRequest request) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        requireOrder(resolvedCampId, orderId);
        List<OrderGuestSaveItemRequest> guests = request == null ? List.of() : request.getGuests();
        List<OrderGuestSaveItemRequest> normalizedGuests = normalizeGuests(guests);
        replaceGuests(orderId, normalizedGuests);
        LocalDateTime guestRegisteredAt = LocalDateTime.now();
        int updated = orderActionMapper.updateGuestRegisteredAt(resolvedCampId, orderId, guestRegisteredAt, userId);
        if (updated != 1) {
            throw new BusinessException(40401, "订单不存在");
        }
        return response(orderId, null, normalizedGuests.size(), "入住人保存成功");
    }

    @Override
    @Transactional
    public OrderChangeRoomOptionsResponseVO getChangeRoomOptions(Long campId, Long orderId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        OrderActionRowVO row = requireOrder(resolvedCampId, orderId);
        validateChangeRoomSourceOrder(row);

        LocalDate blockStartDate = row.getStartAt().toLocalDate();
        LocalDate blockEndDate = resolveBlockEndDate(row.getOrderType(), row.getEndAt());
        List<OrderChangeRoomOptionVO> rooms = orderActionMapper.selectChangeRoomOptions(
                resolvedCampId,
                row.getPoiId(),
                row.getRoomCategoryId(),
                row.getRoomId(),
                row.getStartAt(),
                row.getEndAt(),
                blockStartDate,
                blockEndDate,
                orderId
        );

        OrderChangeRoomOptionsResponseVO response = new OrderChangeRoomOptionsResponseVO();
        response.setOrderId(String.valueOf(orderId));
        response.setRoomId(stringValue(row.getRoomId()));
        response.setRoomName(row.getRoomName());
        response.setRoomCategoryId(stringValue(row.getRoomCategoryId()));
        response.setRoomCategoryName(row.getRoomCategoryName());
        response.setRooms(rooms);
        return response;
    }

    @Override
    @Transactional
    public OrderActionResponseVO changeRoom(Long campId, Long orderId, Long targetRoomId, Long userId, String reason) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        OrderActionRowVO row = requireOrder(resolvedCampId, orderId);
        validateChangeRoomSourceOrder(row);
        if (targetRoomId == null) {
            throw new BusinessException(40001, "目标房间不能为空");
        }
        if (targetRoomId.equals(row.getRoomId())) {
            throw new BusinessException(40001, "目标房间不能与当前房间相同");
        }

        OrderChangeRoomOptionVO targetRoom = orderActionMapper.selectRoomForChangeRoom(
                resolvedCampId,
                row.getPoiId(),
                row.getRoomCategoryId(),
                targetRoomId
        );
        if (targetRoom == null) {
            throw new BusinessException(40001, "目标房间不属于当前房型或不可用");
        }

        LocalDate blockStartDate = row.getStartAt().toLocalDate();
        LocalDate blockEndDate = resolveBlockEndDate(row.getOrderType(), row.getEndAt());
        if (orderActionMapper.countOverlappingClosedRoomBlocks(resolvedCampId, targetRoomId, blockStartDate, blockEndDate) > 0) {
            throw new BusinessException(40001, "目标房间在该时间段已关房，不能换房");
        }
        if (orderActionMapper.countOverlappingActiveOrders(resolvedCampId, targetRoomId, row.getStartAt(), row.getEndAt(), orderId) > 0) {
            throw new BusinessException(40001, "目标房间在该时间段已被占用");
        }

        String roomSnapshotJson = buildChangedRoomSnapshotJson(row, targetRoom);
        String remark = appendChangeRoomRemark(row.getRemark(), reason);
        int updated = orderActionMapper.updateOrderRoom(
                resolvedCampId,
                orderId,
                targetRoomId,
                targetRoom.getRoomName(),
                roomSnapshotJson,
                remark,
                userId
        );
        if (updated != 1) {
            throw new BusinessException(40401, "订单不存在");
        }

        OrderActionResponseVO response = response(orderId, row.getStatus(), null, "换房成功");
        response.setRoomId(targetRoom.getRoomId());
        response.setRoomName(targetRoom.getRoomName());
        response.setRoomCategoryId(targetRoom.getRoomCategoryId());
        response.setRoomCategoryName(targetRoom.getRoomCategoryName());
        return response;
    }

    private void replaceGuests(Long orderId, List<OrderGuestSaveItemRequest> guests) {
        orderActionMapper.deleteOrderGuests(orderId);
        if (guests == null || guests.isEmpty()) {
            return;
        }
        for (int i = 0; i < guests.size(); i++) {
            OrderGuestSaveItemRequest guest = guests.get(i);
            Long guestId = parseLong(guest.getGuestId());
            if (guestId == null) {
                guestId = orderId * 100 + i + 1;
            }
            orderActionMapper.insertOrderGuest(
                    guestId,
                    orderId,
                    guest.getGuestName(),
                    guest.getGuestMobile(),
                    guest.getGuestIdCardType(),
                    sensitiveDataCipher.encryptIdCard(trimToNull(guest.getGuestIdCard())),
                    defaultString(guest.getGuestType(), "adult")
            );
        }
    }

    private List<OrderGuestSaveItemRequest> normalizeGuests(List<OrderGuestSaveItemRequest> guests) {
        if (guests == null || guests.isEmpty()) {
            return List.of();
        }
        return guests.stream().map(this::normalizeGuest).toList();
    }

    private OrderGuestSaveItemRequest normalizeGuest(OrderGuestSaveItemRequest guest) {
        if (guest == null) {
            throw new BusinessException(40001, "入住人信息不能为空");
        }
        OrderGuestSaveItemRequest normalized = new OrderGuestSaveItemRequest();
        normalized.setGuestId(guest.getGuestId());
        normalized.setGuestName(normalizePersonName(guest.getGuestName()));
        normalized.setGuestMobile(normalizeOptionalMainlandMobile(guest.getGuestMobile()));
        String credentialType = InputValidationUtils.normalizeCredentialType(guest.getGuestIdCardType());
        String credentialNumber = trimToNull(guest.getGuestIdCard());
        if (!InputValidationUtils.isValidCredential(credentialType, credentialNumber)) {
            throw new BusinessException(40001, credentialErrorMessage(credentialType));
        }
        normalized.setGuestIdCardType(credentialType);
        normalized.setGuestIdCard(credentialNumber);
        normalized.setGuestType(defaultString(guest.getGuestType(), "adult"));
        return normalized;
    }

    private String normalizePersonName(String value) {
        String normalized = trimToNull(value);
        if (!InputValidationUtils.isValidPersonName(normalized)) {
            throw new BusinessException(40001, "姓名格式不正确，请输入 2-30 个中文或英文字母");
        }
        return normalized;
    }

    private String normalizeOptionalMainlandMobile(String value) {
        String normalized = trimToNull(value);
        if (!InputValidationUtils.isValidOptionalMainlandMobile(normalized)) {
            throw new BusinessException(40001, "手机号格式不正确");
        }
        return normalized;
    }

    private String credentialErrorMessage(String credentialType) {
        if ("居民身份证".equals(credentialType)) {
            return "居民身份证号格式不正确";
        }
        return "证件号码格式不正确";
    }

    private OrderActionRowVO requireOrder(Long campId, Long orderId) {
        if (orderId == null) {
            throw new BusinessException(40001, "orderId is required");
        }
        OrderActionRowVO row = orderActionMapper.selectOrderForUpdate(campId, orderId);
        if (row == null) {
            throw new BusinessException(40401, "订单不存在");
        }
        return row;
    }

    private void validateChangeRoomSourceOrder(OrderActionRowVO row) {
        if (!STATUS_BOOKED.equals(row.getStatus()) && !STATUS_CHECKED_IN.equals(row.getStatus())) {
            throw new BusinessException(40001, "只有预订中或入住中的订单可以换房");
        }
        if (row.getPoiId() == null || row.getRoomCategoryId() == null || row.getRoomId() == null) {
            throw new BusinessException(40001, "订单缺少当前房间信息，不能换房");
        }
        if (row.getStartAt() == null || row.getEndAt() == null || !row.getEndAt().isAfter(row.getStartAt())) {
            throw new BusinessException(40001, "订单入住时间不完整，不能换房");
        }
    }

    private String buildChangedRoomSnapshotJson(OrderActionRowVO row, OrderChangeRoomOptionVO targetRoom) {
        List<Map<String, Object>> snapshots = readRoomSnapshots(row.getRoomSnapshotJson());
        if (snapshots.isEmpty()) {
            snapshots.add(new LinkedHashMap<>());
        }
        Map<String, Object> firstSnapshot = snapshots.get(0);
        firstSnapshot.put("roomCategoryId", targetRoom.getRoomCategoryId());
        firstSnapshot.put("roomType", targetRoom.getRoomCategoryName());
        firstSnapshot.put("roomCategoryName", targetRoom.getRoomCategoryName());
        firstSnapshot.put("roomId", targetRoom.getRoomId());
        firstSnapshot.put("roomName", targetRoom.getRoomName());
        return toJson(snapshots);
    }

    private String buildReleasedRoomSnapshotJson(OrderActionRowVO row) {
        List<Map<String, Object>> snapshots = readRoomSnapshots(row.getRoomSnapshotJson());
        if (snapshots.isEmpty()) {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("roomCategoryId", stringValue(row.getRoomCategoryId()));
            snapshot.put("roomType", row.getRoomCategoryName());
            snapshot.put("roomCategoryName", row.getRoomCategoryName());
            snapshots.add(snapshot);
            return toJson(snapshots);
        }
        for (Map<String, Object> snapshot : snapshots) {
            snapshot.remove("roomId");
            snapshot.remove("roomInfoId");
            snapshot.remove("roomName");
            snapshot.remove("roomNo");
            snapshot.remove("roomLabel");
        }
        return toJson(snapshots);
    }

    private List<Map<String, Object>> readRoomSnapshots(String snapshotJson) {
        if (snapshotJson == null || snapshotJson.isBlank()) {
            return new java.util.ArrayList<>();
        }
        try {
            return objectMapper.readValue(
                    snapshotJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
        } catch (JsonProcessingException exception) {
            throw new BusinessException(40001, "订单房间快照格式错误，不能换房");
        }
    }

    private String appendChangeRoomRemark(String remark, String reason) {
        String normalizedReason = trimToNull(reason);
        if (normalizedReason == null) {
            return remark;
        }
        String changeRoomRemark = "换房原因：" + normalizedReason;
        String existingRemark = trimToNull(remark);
        if (existingRemark == null) {
            return changeRoomRemark;
        }
        return existingRemark + "\n" + changeRoomRemark;
    }

    private String appendSkipStockRemark(String remark, String reason) {
        String normalizedReason = trimToNull(reason);
        if (normalizedReason == null) {
            return remark;
        }
        String skipStockRemark = "不占库存原因：" + normalizedReason;
        String existingRemark = trimToNull(remark);
        if (existingRemark == null) {
            return skipStockRemark;
        }
        return existingRemark + "\n" + skipStockRemark;
    }

    private String appendNoShowRemark(String remark, String reason) {
        String normalizedReason = trimToNull(reason);
        if (normalizedReason == null) {
            return remark;
        }
        String noShowRemark = "未到店原因：" + normalizedReason;
        String existingRemark = trimToNull(remark);
        if (existingRemark == null) {
            return noShowRemark;
        }
        return existingRemark + "\n" + noShowRemark;
    }

    private void updateStatus(
            Long campId,
            Long orderId,
            String status,
            String paymentStatus,
            String remark,
            LocalDateTime guestRegisteredAt,
            LocalDateTime checkedOutAt,
            Long userId
    ) {
        int updated = orderActionMapper.updateOrderStatus(
                campId, orderId, status, paymentStatus, remark, guestRegisteredAt, checkedOutAt, userId
        );
        if (updated != 1) {
            throw new BusinessException(40401, "订单不存在");
        }
    }

    private OrderActionResponseVO response(Long orderId, String status, Integer guestCount, String message) {
        return response(orderId, status, guestCount, null, null, message);
    }

    private OrderActionResponseVO response(
            Long orderId,
            String status,
            Integer guestCount,
            LocalDateTime guestRegisteredAt,
            LocalDateTime checkedOutAt,
            String message
    ) {
        OrderActionRowVO orderTimes = orderActionMapper.selectOrderTimes(orderId);
        LocalDateTime resolvedGuestRegisteredAt = guestRegisteredAt != null
                ? guestRegisteredAt
                : orderTimes == null ? null : orderTimes.getGuestRegisteredAt();
        LocalDateTime resolvedCheckedOutAt = checkedOutAt != null
                ? checkedOutAt
                : orderTimes == null ? null : orderTimes.getCheckedOutAt();
        OrderActionResponseVO response = new OrderActionResponseVO();
        response.setOrderId(String.valueOf(orderId));
        response.setStatus(status);
        response.setGuestCount(guestCount);
        response.setGuestRegisteredAt(formatDateTime(resolvedGuestRegisteredAt));
        response.setCheckedOutAt(formatDateTime(resolvedCheckedOutAt));
        response.setMessage(message);
        return response;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : DATE_TIME_FORMATTER.format(value);
    }

    private void validateRoomSelection(
            Long campId,
            Long poiId,
            Long roomCategoryId,
            Long roomId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Long orderId,
            String orderType
    ) {
        if (!requiresRoomSelection(orderType)) {
            return;
        }
        if (poiId == null || roomCategoryId == null || roomId == null) {
            throw new BusinessException(40001, "请选择可用房间后创建订单");
        }
        if (orderActionMapper.countActiveRoomBySelection(campId, poiId, roomCategoryId, roomId) != 1) {
            throw new BusinessException(40001, "所选房间不属于当前门店或房型");
        }
        LocalDate blockStartDate = startAt.toLocalDate();
        LocalDate blockEndDate = resolveBlockEndDate(orderType, endAt);
        if (orderActionMapper.countOverlappingClosedRoomBlocks(campId, roomId, blockStartDate, blockEndDate) > 0) {
            throw new BusinessException(40001, "所选房间在该时间段已关房，不能录单");
        }
        if (orderActionMapper.countOverlappingActiveOrders(campId, roomId, startAt, endAt, orderId) > 0) {
            throw new BusinessException(40001, "所选房间在该时间段已被占用");
        }
    }

    private LocalDate resolveBlockEndDate(String orderType, LocalDateTime endAt) {
        if ("hourly_room".equals(orderType)) {
            return endAt.minusNanos(1).toLocalDate();
        }
        return endAt.toLocalDate().minusDays(1);
    }

    private boolean requiresRoomSelection(String orderType) {
        return "daily_room".equals(orderType) || "hourly_room".equals(orderType) || "long_rental".equals(orderType);
    }

    private List<OrderCreateRoomItemRequest> normalizeRoomSnapshots(
            List<OrderCreateRoomItemRequest> rooms,
            Long roomCategoryId,
            Long roomId
    ) {
        if (rooms == null || rooms.isEmpty()) {
            return rooms;
        }
        String roomCategoryIdText = roomCategoryId == null ? null : String.valueOf(roomCategoryId);
        String roomIdText = roomId == null ? null : String.valueOf(roomId);
        for (OrderCreateRoomItemRequest room : rooms) {
            if (room == null) {
                continue;
            }
            if (trimToNull(room.getRoomCategoryId()) == null) {
                room.setRoomCategoryId(roomCategoryIdText);
            }
            if (trimToNull(room.getRoomId()) == null) {
                room.setRoomId(roomIdText);
            }
        }
        return rooms;
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        Long currentCampId = userCampMapper.selectCurrentCampId(userId);
        if (currentCampId == null) {
            throw new BusinessException(40401, "未找到当前用户门店");
        }
        if (requestedCampId == null) {
            return currentCampId;
        }
        if (!requestedCampId.equals(currentCampId)) {
            throw new BusinessException(40301, "无权访问当前门店订单数据");
        }
        return requestedCampId;
    }

    private Long parseRequiredLong(String value, String fieldName) {
        Long parsed = parseLong(value);
        if (parsed == null) {
            throw new BusinessException(40001, fieldName + " is required");
        }
        return parsed;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }

    private LocalDate parseRequiredDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(40001, fieldName + " is required");
        }
        return LocalDate.parse(value);
    }

    private LocalDate parseOptionalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private LocalDateTime parseRequiredDateTime(String value, String fieldName, LocalTime fallbackTime) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(40001, fieldName + " is required");
        }
        String normalized = value.trim();
        if (normalized.length() == 10) {
            return LocalDateTime.of(LocalDate.parse(normalized), fallbackTime);
        }
        return LocalDateTime.parse(normalized, DATE_TIME_FORMATTER);
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(40001, fieldName + " is required");
        }
        return value.trim();
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private String stringValue(Long value) {
        return value == null ? null : String.valueOf(value);
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String normalizeOrderType(String orderType, String stayType) {
        String normalized = trimToNull(orderType);
        if (normalized != null) {
            return normalized;
        }
        if ("longRental".equalsIgnoreCase(trimToNull(stayType))) {
            return "long_rental";
        }
        if ("hourly".equalsIgnoreCase(trimToNull(stayType))) {
            return "hourly_room";
        }
        return "daily_room";
    }

    private String resolveSourceType(String sourceLabel) {
        return switch (defaultString(sourceLabel, DEFAULT_LOCAL_CHANNEL_NAME)) {
            case "携程", "飞猪", "美团" -> "channel";
            case "电话订单" -> "phone";
            case "企业客户" -> "company";
            default -> "frontdesk";
        };
    }

    private Long resolvePaymentWayId(String payMethod) {
        return switch (defaultString(payMethod, "wechat")) {
            case "cash" -> 17201L;
            case "wechat" -> 17202L;
            case "alipay" -> 17203L;
            case "platform" -> 17206L;
            default -> 17202L;
        };
    }

    private long resolveCommissionPrice(OrderCreateRequest request) {
        if (request.getCommissionPrice() != null) {
            return request.getCommissionPrice();
        }
        long totalPrice = defaultLong(request.getTotalPrice());
        long totalPay = defaultLong(request.getTotalPayPrice());
        long otherPrice = resolveOtherPrice(request);
        return Math.max(totalPrice - totalPay - otherPrice, 0L);
    }

    private int resolveDayNum(String orderType, LocalDateTime startAt, LocalDateTime endAt) {
        if ("hourly_room".equals(orderType)) {
            long hours = Math.max(java.time.Duration.between(startAt, endAt).toHours(), 1L);
            return Math.toIntExact(hours);
        }
        return Math.max(1, Math.toIntExact(endAt.toLocalDate().toEpochDay() - startAt.toLocalDate().toEpochDay()));
    }

    private long resolveOtherPrice(OrderCreateRequest request) {
        if (request.getOtherPrice() != null) {
            return request.getOtherPrice();
        }
        return defaultLong(request.getExtraFee());
    }

    private String defaultJson(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(40001, "request json serialization failed");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private long generateOrderId() {
        long base = System.currentTimeMillis();
        long randomTail = Math.abs(java.util.UUID.randomUUID().hashCode() % 1000);
        return base * 1000 + randomTail;
    }
}
