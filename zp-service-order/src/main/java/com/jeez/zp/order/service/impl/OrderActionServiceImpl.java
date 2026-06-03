package com.jeez.zp.order.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeez.zp.order.dto.request.OrderCreateRequest;
import com.jeez.zp.order.dto.request.OrderGuestSaveItemRequest;
import com.jeez.zp.order.dto.request.OrderGuestsSaveRequest;
import com.jeez.zp.order.exception.BusinessException;
import com.jeez.zp.order.mapper.OrderActionMapper;
import com.jeez.zp.order.mapper.UserCampMapper;
import com.jeez.zp.order.service.OrderActionService;
import com.jeez.zp.order.vo.OrderActionResponseVO;
import com.jeez.zp.order.vo.OrderActionRowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderActionServiceImpl implements OrderActionService {

    private static final String STATUS_BOOKED = "booked";
    private static final String STATUS_CHECKED_IN = "checked_in";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_CANCELLED = "cancelled";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OrderActionMapper orderActionMapper;
    private final UserCampMapper userCampMapper;
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
        String sourceLabel = defaultString(request.getSourceLabel(), "自来客");

        orderActionMapper.insertOrderMain(
                orderId,
                campId,
                poiId,
                roomCategoryId,
                roomId,
                orderType,
                "ORDER-ACTION-" + orderId,
                defaultString(request.getChannelOrderNo(), "OUT-ACTION-" + orderId),
                requireText(request.getGuestName(), "guestName"),
                request.getGuestMobile(),
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
                toJson(request.getRooms()),
                toJson(request.getTags()),
                toJson(request.getReminders()),
                toJson(request.getExtraFeeItems()),
                defaultJson(request.getBillingSnapshot()),
                request.getRemark(),
                userId
        );
        replaceGuests(orderId, request.getGuests());
        return response(orderId, STATUS_BOOKED, request.getGuests() == null ? 0 : request.getGuests().size(), "订单创建成功");
    }

    @Override
    @Transactional
    public OrderActionResponseVO cancelOrder(Long campId, Long orderId, Long userId, String reason) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        OrderActionRowVO row = requireOrder(resolvedCampId, orderId);
        if (STATUS_COMPLETED.equals(row.getStatus())) {
            throw new BusinessException(40001, "completed order cannot be cancelled");
        }
        updateStatus(resolvedCampId, orderId, STATUS_CANCELLED, "cancelled", reason, userId);
        return response(orderId, STATUS_CANCELLED, null, "订单取消成功");
    }

    @Override
    @Transactional
    public OrderActionResponseVO checkIn(Long campId, Long orderId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        OrderActionRowVO row = requireOrder(resolvedCampId, orderId);
        if (!STATUS_BOOKED.equals(row.getStatus())) {
            throw new BusinessException(40001, "only booked order can check in");
        }
        updateStatus(resolvedCampId, orderId, STATUS_CHECKED_IN, null, null, userId);
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
        updateStatus(resolvedCampId, orderId, STATUS_COMPLETED, "paid", null, userId);
        return response(orderId, STATUS_COMPLETED, null, "办理退房成功");
    }

    @Override
    @Transactional
    public OrderActionResponseVO saveGuests(Long campId, Long orderId, Long userId, OrderGuestsSaveRequest request) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        requireOrder(resolvedCampId, orderId);
        List<OrderGuestSaveItemRequest> guests = request == null ? List.of() : request.getGuests();
        replaceGuests(orderId, guests);
        return response(orderId, null, guests == null ? 0 : guests.size(), "入住人保存成功");
    }

    private void replaceGuests(Long orderId, List<OrderGuestSaveItemRequest> guests) {
        orderActionMapper.deleteOrderGuests(orderId);
        if (guests == null) {
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
                    requireText(guest.getGuestName(), "guestName"),
                    guest.getGuestMobile(),
                    guest.getGuestIdCardType(),
                    guest.getGuestIdCard(),
                    defaultString(guest.getGuestType(), "adult")
            );
        }
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

    private void updateStatus(Long campId, Long orderId, String status, String paymentStatus, String remark, Long userId) {
        int updated = orderActionMapper.updateOrderStatus(campId, orderId, status, paymentStatus, remark, userId);
        if (updated != 1) {
            throw new BusinessException(40401, "订单不存在");
        }
    }

    private OrderActionResponseVO response(Long orderId, String status, Integer guestCount, String message) {
        OrderActionResponseVO response = new OrderActionResponseVO();
        response.setOrderId(String.valueOf(orderId));
        response.setStatus(status);
        response.setGuestCount(guestCount);
        response.setMessage(message);
        return response;
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
        return switch (defaultString(sourceLabel, "自来客")) {
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
