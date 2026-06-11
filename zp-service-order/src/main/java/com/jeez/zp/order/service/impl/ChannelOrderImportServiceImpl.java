package com.jeez.zp.order.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeez.common.utils.InputValidationUtils;
import com.jeez.zp.order.dto.request.ChannelOrderImportRequest;
import com.jeez.zp.order.exception.BusinessException;
import com.jeez.zp.order.mapper.ChannelOrderMapper;
import com.jeez.zp.order.mapper.OrderActionMapper;
import com.jeez.zp.order.mapper.UserCampMapper;
import com.jeez.zp.order.service.ChannelOrderImportService;
import com.jeez.zp.order.vo.ChannelOrderAccountVO;
import com.jeez.zp.order.vo.ChannelOrderAvailableRoomVO;
import com.jeez.zp.order.vo.ChannelOrderImportResponseVO;
import com.jeez.zp.order.vo.ChannelOrderMappingVO;
import com.jeez.zp.order.vo.ChannelOrderRawVO;
import com.jeez.zp.order.vo.OrderActionRowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChannelOrderImportServiceImpl implements ChannelOrderImportService {

    private static final int MAX_INTERNAL_OUT_ORDER_NO_LENGTH = 64;
    private static final String STATUS_BOOKED = "booked";
    private static final String IMPORT_STATUS_SUCCESS = "success";
    private static final String IMPORT_STATUS_FAILED = "failed";
    private static final String IDEMPOTENT_RETRY_MESSAGE = "外部订单号已导入，本次未重复创建订单";
    private static final String IDEMPOTENT_MISMATCH_MESSAGE = "外部订单号已导入，但本次请求内容与原订单不一致，请更换 outOrderNo 或取消原订单后重试";
    private static final String IDEMPOTENT_AUTO_ARRANGED_MESSAGE = "外部订单号已导入，本次未重复创建订单，并已自动排房";

    private final ChannelOrderMapper channelOrderMapper;
    private final OrderActionMapper orderActionMapper;
    private final UserCampMapper userCampMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public ChannelOrderImportResponseVO importOrder(ChannelOrderImportRequest request, Long userId) {
        Long accountId = parseRequiredLong(request == null ? null : request.getAccountId(), "accountId");
        String outOrderNo = requireText(request.getOutOrderNo(), "outOrderNo");
        String rawPayloadJson = toJson(request.getRawPayload() == null ? request : request.getRawPayload());
        ChannelOrderAccountVO account = loadAuthorizedAccount(accountId);
        String channelCode = resolveChannelCode(request.getChannelCode(), account);
        Long campId = resolveAccessibleCampId(account.getCampId(), userId);
        ChannelOrderRawVO existingRaw = channelOrderMapper.selectRawByExternalOrder(accountId, outOrderNo);
        if (existingRaw != null
                && IMPORT_STATUS_SUCCESS.equals(existingRaw.getImportStatus())
                && existingRaw.getPmsOrderId() != null) {
            validateIdempotentRetryMatchesExistingOrder(existingRaw.getPmsOrderId(), request, account, campId);
            return recoverExistingImportedOrderIfNeeded(existingRaw.getPmsOrderId(), outOrderNo, account, campId, userId);
        }

        Long rawId = existingRaw == null ? generateId() : existingRaw.getRawId();
        recordProcessing(rawId, campId, account, channelCode, outOrderNo, rawPayloadJson);
        try {
            return importNewOrder(request, account, campId, rawPayloadJson);
        } catch (BusinessException exception) {
            recordFailure(accountId, outOrderNo, rawPayloadJson, exception.getMessage());
            throw exception;
        } catch (RuntimeException exception) {
            recordFailure(accountId, outOrderNo, rawPayloadJson, exception.getMessage());
            throw exception;
        }
    }

    protected ChannelOrderImportResponseVO importNewOrder(
            ChannelOrderImportRequest request,
            ChannelOrderAccountVO account,
            Long campId,
            String rawPayloadJson
    ) {
        validateQuantity(request.getQuantity());
        String outOrderNo = requireText(request.getOutOrderNo(), "outOrderNo");
        String outPoiId = requireText(request.getOutPoiId(), "outPoiId");
        String guestName = normalizePersonName(request.getContactName());
        String guestMobile = normalizeOptionalMainlandMobile(request.getContactMobile());
        LocalDate checkInDate = parseRequiredDate(request.getCheckInDate(), "checkInDate");
        LocalDate checkOutDate = parseRequiredDate(request.getCheckOutDate(), "checkOutDate");
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new BusinessException(40001, "离店日期必须晚于入住日期");
        }

        ChannelOrderMappingVO mapping = resolveChannelRoomCategoryMapping(account.getAccountId(), outPoiId, request);
        if (!campId.equals(mapping.getCampId())) {
            throw new BusinessException(40301, "当前用户无权访问该渠道账号");
        }

        LocalDateTime startAt = LocalDateTime.of(checkInDate, LocalTime.of(14, 0));
        LocalDateTime endAt = LocalDateTime.of(checkOutDate, LocalTime.of(12, 0));
        LocalDate blockEndDate = checkOutDate.minusDays(1);
        int availableStock = defaultInt(channelOrderMapper.countAvailableRoomCategoryStock(
                campId,
                mapping.getPoiId(),
                mapping.getRoomCategoryId(),
                startAt,
                endAt,
                checkInDate,
                blockEndDate,
                null
        ));
        if (availableStock < 1) {
            throw new BusinessException(40001, "该渠道房型没有可用库存");
        }
        ChannelOrderAvailableRoomVO autoArrangedRoom = channelOrderMapper.selectAvailableRoomForAutoArrange(
                campId,
                mapping.getPoiId(),
                mapping.getRoomCategoryId(),
                startAt,
                endAt,
                checkInDate,
                blockEndDate,
                null
        );
        if (autoArrangedRoom == null) {
            throw new BusinessException(40001, "该渠道房型没有可自动排房的空房");
        }

        Long orderId = generateId();
        String internalOutOrderNo = buildInternalOutOrderNo(account.getAccountId(), outOrderNo);
        String roomSnapshotJson = buildRoomSnapshotJson(mapping, request, checkInDate, checkOutDate, autoArrangedRoom);
        channelOrderMapper.insertOrderMain(
                orderId,
                campId,
                mapping.getPoiId(),
                mapping.getRoomCategoryId(),
                autoArrangedRoom.getRoomId(),
                account.getAccountId(),
                "CHANNEL-" + orderId,
                internalOutOrderNo,
                outOrderNo,
                guestName,
                guestMobile,
                startAt,
                endAt,
                Math.max(1, Math.toIntExact(checkOutDate.toEpochDay() - checkInDate.toEpochDay())),
                defaultLong(request.getTotalPrice()),
                defaultLong(request.getTotalPayPrice()),
                defaultLong(request.getCommissionPrice()),
                defaultString(request.getPaymentStatus(), "unpaid"),
                account.getChannelName(),
                mapping.getPoiName(),
                mapping.getRoomCategoryName(),
                autoArrangedRoom.getRoomName(),
                roomSnapshotJson,
                request.getRemark(),
                null
        );
        if (defaultLong(request.getCommissionPrice()) > 0) {
            channelOrderMapper.insertDistributionOrder(
                    generateId(),
                    campId,
                    orderId,
                    account.getAccountId(),
                    defaultLong(request.getCommissionPrice())
            );
        }
        channelOrderMapper.updateRawSuccess(account.getAccountId(), outOrderNo, orderId, rawPayloadJson);
        return response(orderId, outOrderNo, account, true, "渠道订单导入成功");
    }

    protected ChannelOrderImportResponseVO recoverExistingImportedOrderIfNeeded(
            Long orderId,
            String outOrderNo,
            ChannelOrderAccountVO account,
            Long campId,
            Long userId
    ) {
        OrderActionRowVO row = orderActionMapper.selectOrderForUpdate(campId, orderId);
        if (row == null) {
            throw new BusinessException(40401, "未找到已导入的渠道订单");
        }
        if (row.getRoomId() != null) {
            return response(orderId, outOrderNo, account, false, IDEMPOTENT_RETRY_MESSAGE);
        }

        LocalDateTime startAt = row.getStartAt();
        LocalDateTime endAt = row.getEndAt();
        LocalDate blockStartDate = startAt.toLocalDate();
        LocalDate blockEndDate = endAt.toLocalDate().minusDays(1);
        int availableStock = defaultInt(channelOrderMapper.countAvailableRoomCategoryStock(
                campId,
                row.getPoiId(),
                row.getRoomCategoryId(),
                startAt,
                endAt,
                blockStartDate,
                blockEndDate,
                row.getOrderId()
        ));
        if (availableStock < 1) {
            throw new BusinessException(40001, "该渠道房型没有可用库存");
        }
        ChannelOrderAvailableRoomVO autoArrangedRoom = channelOrderMapper.selectAvailableRoomForAutoArrange(
                campId,
                row.getPoiId(),
                row.getRoomCategoryId(),
                startAt,
                endAt,
                blockStartDate,
                blockEndDate,
                row.getOrderId()
        );
        if (autoArrangedRoom == null) {
            throw new BusinessException(40001, "该渠道房型没有可自动排房的空房");
        }

        String roomSnapshotJson = buildRoomSnapshotJson(row, autoArrangedRoom);
        orderActionMapper.updateOrderRoom(
                campId,
                orderId,
                autoArrangedRoom.getRoomId(),
                autoArrangedRoom.getRoomName(),
                roomSnapshotJson,
                row.getRemark(),
                userId
        );
        return response(orderId, outOrderNo, account, false, IDEMPOTENT_AUTO_ARRANGED_MESSAGE);
    }

    private void validateIdempotentRetryMatchesExistingOrder(
            Long orderId,
            ChannelOrderImportRequest request,
            ChannelOrderAccountVO account,
            Long campId
    ) {
        OrderActionRowVO row = orderActionMapper.selectOrderForUpdate(campId, orderId);
        if (row == null) {
            throw new BusinessException(40401, "未找到已导入的渠道订单");
        }

        String outPoiId = requireText(request.getOutPoiId(), "outPoiId");
        LocalDate checkInDate = parseRequiredDate(request.getCheckInDate(), "checkInDate");
        LocalDate checkOutDate = parseRequiredDate(request.getCheckOutDate(), "checkOutDate");
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new BusinessException(40001, "离店日期必须晚于入住日期");
        }

        boolean mismatched = hasIncomingRoomMappingMismatch(row, account.getAccountId(), outPoiId, request)
                || !Objects.equals(row.getStartAt(), LocalDateTime.of(checkInDate, LocalTime.of(14, 0)))
                || !Objects.equals(row.getEndAt(), LocalDateTime.of(checkOutDate, LocalTime.of(12, 0)))
                || !Objects.equals(row.getGuestName(), normalizePersonName(request.getContactName()))
                || !Objects.equals(row.getGuestMobile(), normalizeOptionalMainlandMobile(request.getContactMobile()))
                || !Objects.equals(defaultLong(row.getTotalPriceCent()), defaultLong(request.getTotalPrice()))
                || !Objects.equals(defaultLong(row.getTotalPayPriceCent()), defaultLong(request.getTotalPayPrice()))
                || !Objects.equals(defaultLong(row.getCommissionPriceCent()), defaultLong(request.getCommissionPrice()))
                || !Objects.equals(defaultString(row.getPaymentStatus(), "unpaid"), defaultString(request.getPaymentStatus(), "unpaid"));
        if (mismatched) {
            throw new BusinessException(40001, IDEMPOTENT_MISMATCH_MESSAGE);
        }
    }

    private boolean hasIncomingRoomMappingMismatch(
            OrderActionRowVO row,
            Long accountId,
            String outPoiId,
            ChannelOrderImportRequest request
    ) {
        try {
            ChannelOrderMappingVO mapping = resolveChannelRoomCategoryMapping(accountId, outPoiId, request);
            return !Objects.equals(row.getPoiId(), mapping.getPoiId())
                    || !Objects.equals(row.getRoomCategoryId(), mapping.getRoomCategoryId());
        } catch (BusinessException exception) {
            return true;
        }
    }

    private ChannelOrderMappingVO resolveChannelRoomCategoryMapping(
            Long accountId,
            String outPoiId,
            ChannelOrderImportRequest request
    ) {
        String outRoomCategoryId = InputValidationUtils.trimToNull(request.getOutRoomCategoryId());
        if (outRoomCategoryId != null) {
            ChannelOrderMappingVO mapping = channelOrderMapper.selectMapping(accountId, outPoiId, outRoomCategoryId);
            if (mapping == null) {
                throw new BusinessException(40001, "未找到渠道房型映射，请检查 outPoiId 和 outRoomCategoryId 是否已关联");
            }
            return mapping;
        }

        String roomCategoryName = requireText(request.getRoomCategoryName(), "roomCategoryName");
        List<ChannelOrderMappingVO> mappings = channelOrderMapper.selectMappingsByRoomCategoryName(
                accountId,
                outPoiId,
                roomCategoryName
        );
        if (mappings == null || mappings.isEmpty()) {
            throw new BusinessException(40001, "未找到渠道房型映射，请检查 roomCategoryName 是否已在该渠道门店下关联");
        }
        if (mappings.size() > 1) {
            throw new BusinessException(40001, "房型名称匹配到多个渠道房型，请改用 outRoomCategoryId");
        }
        return mappings.get(0);
    }

    protected void recordProcessing(
            Long rawId,
            Long campId,
            ChannelOrderAccountVO account,
            String channelCode,
            String outOrderNo,
            String rawPayloadJson
    ) {
        channelOrderMapper.upsertRawProcessing(
                rawId,
                campId,
                account.getAccountId(),
                account.getChannelId(),
                channelCode,
                outOrderNo,
                rawPayloadJson
        );
    }

    protected void recordFailure(Long accountId, String outOrderNo, String rawPayloadJson, String errorMessage) {
        channelOrderMapper.updateRawFailure(
                accountId,
                outOrderNo,
                rawPayloadJson,
                truncate(errorMessage, 255)
        );
    }

    private ChannelOrderAccountVO loadAuthorizedAccount(Long accountId) {
        ChannelOrderAccountVO account = channelOrderMapper.selectAccount(accountId);
        if (account == null) {
            throw new BusinessException(40001, "渠道账号不存在");
        }
        if (!"authorized".equals(account.getStatus())) {
            throw new BusinessException(40001, "渠道账号未授权");
        }
        return account;
    }

    private String resolveChannelCode(String requestChannelCode, ChannelOrderAccountVO account) {
        String normalizedRequestCode = requireText(requestChannelCode, "channelCode");
        if ("meituan".equals(normalizedRequestCode)) {
            throw new BusinessException(
                    40001,
                    "channelCode=meituan 无法区分美团酒店和美团民宿，请使用 meituan_hotel 或 meituan_homestay"
            );
        }

        String accountChannelCode = InputValidationUtils.trimToNull(account.getChannelCode());
        if (accountChannelCode == null) {
            throw new BusinessException(40001, "渠道账号未配置 channelCode");
        }
        if (!accountChannelCode.equals(normalizedRequestCode)) {
            throw new BusinessException(40001, "请求的 channelCode 与渠道账号配置不一致");
        }
        return normalizedRequestCode;
    }

    private Long resolveAccessibleCampId(Long accountCampId, Long userId) {
        Long currentCampId = userCampMapper.selectCurrentCampId(userId);
        if (currentCampId == null) {
            throw new BusinessException(40401, "未找到当前用户门店");
        }
        if (!currentCampId.equals(accountCampId)) {
            throw new BusinessException(40301, "当前用户无权访问该渠道账号");
        }
        return currentCampId;
    }

    private void validateQuantity(Integer quantity) {
        int normalizedQuantity = quantity == null ? 1 : quantity;
        if (normalizedQuantity != 1) {
            throw new BusinessException(40001, "当前渠道导入阶段只支持 quantity=1");
        }
    }

    private String buildInternalOutOrderNo(Long accountId, String outOrderNo) {
        String value = accountId + ":" + outOrderNo;
        if (value.length() > MAX_INTERNAL_OUT_ORDER_NO_LENGTH) {
            throw new BusinessException(40001, "accountId 与 outOrderNo 拼接后过长，无法写入订单外部单号");
        }
        return value;
    }

    private String buildRoomSnapshotJson(
            ChannelOrderMappingVO mapping,
            ChannelOrderImportRequest request,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            ChannelOrderAvailableRoomVO autoArrangedRoom
    ) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("roomCategoryId", String.valueOf(mapping.getRoomCategoryId()));
        snapshot.put("roomCategoryName", mapping.getRoomCategoryName());
        snapshot.put("roomType", mapping.getRoomCategoryName());
        snapshot.put("roomId", String.valueOf(autoArrangedRoom.getRoomId()));
        snapshot.put("roomName", autoArrangedRoom.getRoomName());
        snapshot.put("quantity", 1);
        snapshot.put("checkInDate", checkInDate.toString());
        snapshot.put("checkOutDate", checkOutDate.toString());
        snapshot.put("price", defaultLong(request.getTotalPrice()));
        return toJson(List.of(snapshot));
    }

    private String buildRoomSnapshotJson(OrderActionRowVO row, ChannelOrderAvailableRoomVO autoArrangedRoom) {
        List<Map<String, Object>> snapshots = readRoomSnapshots(row.getRoomSnapshotJson());
        if (snapshots.isEmpty()) {
            snapshots.add(new LinkedHashMap<>());
        }
        Map<String, Object> firstSnapshot = snapshots.get(0);
        firstSnapshot.put("roomCategoryId", stringValue(row.getRoomCategoryId()));
        firstSnapshot.put("roomCategoryName", row.getRoomCategoryName());
        firstSnapshot.put("roomType", row.getRoomCategoryName());
        firstSnapshot.put("roomId", stringValue(autoArrangedRoom.getRoomId()));
        firstSnapshot.put("roomName", autoArrangedRoom.getRoomName());
        if (row.getStartAt() != null) {
            firstSnapshot.put("checkInDate", row.getStartAt().toLocalDate().toString());
        }
        if (row.getEndAt() != null) {
            firstSnapshot.put("checkOutDate", row.getEndAt().toLocalDate().toString());
        }
        if (!firstSnapshot.containsKey("quantity")) {
            firstSnapshot.put("quantity", 1);
        }
        return toJson(snapshots);
    }

    private ChannelOrderImportResponseVO response(
            Long orderId,
            String outOrderNo,
            ChannelOrderAccountVO account,
            boolean created,
            String message
    ) {
        ChannelOrderImportResponseVO response = new ChannelOrderImportResponseVO();
        response.setOrderId(String.valueOf(orderId));
        response.setOutOrderNo(outOrderNo);
        response.setAccountId(String.valueOf(account.getAccountId()));
        response.setChannelName(account.getChannelName());
        response.setStatus(STATUS_BOOKED);
        response.setCreated(created);
        response.setMessage(message);
        return response;
    }

    private String normalizePersonName(String value) {
        String normalized = InputValidationUtils.trimToNull(value);
        if (!InputValidationUtils.isValidPersonName(normalized)) {
            throw new BusinessException(40001, "联系人姓名格式不正确");
        }
        return normalized;
    }

    private String normalizeOptionalMainlandMobile(String value) {
        String normalized = InputValidationUtils.trimToNull(value);
        if (!InputValidationUtils.isValidOptionalMainlandMobile(normalized)) {
            throw new BusinessException(40001, "联系人手机号格式不正确");
        }
        return normalized;
    }

    private Long parseRequiredLong(String value, String fieldName) {
        String normalized = InputValidationUtils.trimToNull(value);
        if (normalized == null) {
            throw new BusinessException(40001, requiredFieldMessage(fieldName));
        }
        return Long.valueOf(normalized);
    }

    private LocalDate parseRequiredDate(String value, String fieldName) {
        String normalized = InputValidationUtils.trimToNull(value);
        if (normalized == null) {
            throw new BusinessException(40001, requiredFieldMessage(fieldName));
        }
        return LocalDate.parse(normalized);
    }

    private String requireText(String value, String fieldName) {
        String normalized = InputValidationUtils.trimToNull(value);
        if (normalized == null) {
            throw new BusinessException(40001, requiredFieldMessage(fieldName));
        }
        return normalized;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(40001, "请求报文序列化失败");
        }
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
            throw new BusinessException(40001, "房间快照格式不正确");
        }
    }

    private String requiredFieldMessage(String fieldName) {
        return "缺少必填字段: " + fieldName;
    }

    private String stringValue(Long value) {
        return value == null ? null : String.valueOf(value);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private long generateId() {
        long base = System.currentTimeMillis();
        long randomTail = Math.abs(java.util.UUID.randomUUID().hashCode() % 1000);
        return base * 1000 + randomTail;
    }
}
