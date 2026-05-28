package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.dto.request.OrderLedgerDashboardRequest;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.OrderLedgerMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.RoomMapper;
import com.jeez.zp.platform.service.OrderLedgerService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.OrderLedgerDashboardVO;
import com.jeez.zp.platform.vo.OrderLedgerDetailVO;
import com.jeez.zp.platform.vo.OrderLedgerExtraLineVO;
import com.jeez.zp.platform.vo.OrderLedgerOptionVO;
import com.jeez.zp.platform.vo.OrderLedgerPaginationVO;
import com.jeez.zp.platform.vo.OrderLedgerPaymentRecordVO;
import com.jeez.zp.platform.vo.OrderLedgerQueryRowVO;
import com.jeez.zp.platform.vo.OrderLedgerRecordVO;
import com.jeez.zp.platform.vo.OrderLedgerRoomBreakdownVO;
import com.jeez.zp.platform.vo.OrderLedgerStoreOptionVO;
import com.jeez.zp.platform.vo.OrderLedgerSummaryQueryVO;
import com.jeez.zp.platform.vo.OrderLedgerSummaryVO;
import com.jeez.zp.platform.vo.RoomCategoryRoomsGroupVO;
import com.jeez.zp.platform.vo.RoomItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderLedgerServiceImpl implements OrderLedgerService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final OrderLedgerMapper orderLedgerMapper;
    private final RoomMapper roomMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public OrderLedgerDashboardVO getDashboard(OrderLedgerDashboardRequest request, Long userId) {
        Long campId = resolveAccessibleCampId(parseNullableLong(request.getCampId()), userId);
        int pageNum = normalizePageNum(request.getPageNum());
        int pageSize = normalizePageSize(request.getPageSize());
        long offset = (long) (pageNum - 1) * pageSize;

        LocalDateTime beginTime = parseDateStart(request.getBeginTime());
        LocalDateTime endTimeExclusive = parseDateEndExclusive(request.getEndTime());
        Integer isIncome = normalizeIncome(request.getIsIncome());
        Integer type = normalizeType(request.getType());
        List<Long> paymentTypeIds = parseLongList(request.getPaymentTypeIds());
        List<Long> paymentWayIds = parseLongList(request.getPaymentWayIds());
        List<Long> roomIds = parseLongList(request.getRoomIds());
        List<Long> poiIds = parseLongList(request.getPoiIds());
        String keyword = normalizeKeyword(request.getKeyword());

        long total = orderLedgerMapper.countPage(
                campId,
                beginTime,
                endTimeExclusive,
                paymentTypeIds,
                paymentWayIds,
                roomIds,
                poiIds,
                keyword,
                isIncome,
                type
        );
        OrderLedgerSummaryQueryVO summaryQuery = orderLedgerMapper.selectSummary(
                campId,
                beginTime,
                endTimeExclusive,
                paymentTypeIds,
                paymentWayIds,
                roomIds,
                poiIds,
                keyword,
                isIncome,
                type
        );
        List<OrderLedgerQueryRowVO> rows = total == 0
                ? List.of()
                : orderLedgerMapper.selectPage(
                        campId,
                        beginTime,
                        endTimeExclusive,
                        paymentTypeIds,
                        paymentWayIds,
                        roomIds,
                        poiIds,
                        keyword,
                        isIncome,
                        type,
                        offset,
                        pageSize
                );

        OrderLedgerDashboardVO dashboard = new OrderLedgerDashboardVO();
        dashboard.setProvider("api");
        dashboard.setState("success");
        dashboard.setRequest(request);
        dashboard.setStores(orderLedgerMapper.selectStores(campId));
        dashboard.setTypeOptions(typeOptions());
        dashboard.setSourceOptions(sourceOptions());
        dashboard.setProjectOptions(orderLedgerMapper.selectProjectOptions(campId, isIncome));
        dashboard.setPaymentWayOptions(orderLedgerMapper.selectPaymentWayOptions(campId));
        dashboard.setRoomOptions(selectRoomOptions(campId));
        dashboard.setSummary(toSummary(summaryQuery));
        dashboard.setRecords(rows.stream().map(this::toRecord).toList());
        dashboard.setPagination(toPagination(pageNum, pageSize, total));
        dashboard.setUpdatedAt(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        dashboard.setTraceIds(List.of("order-ledger-dashboard-get"));
        return dashboard;
    }

    private List<RoomCategoryRoomsGroupVO> selectRoomOptions(Long campId) {
        List<RoomCategoryRoomsGroupVO> groups = roomMapper.selectRoomCategoryGroups(campId, List.of());
        if (groups.isEmpty()) {
            return List.of();
        }
        Map<String, List<RoomItemVO>> roomItemsByCategoryId = roomMapper.selectRooms(campId, List.of(), null).stream()
                .collect(Collectors.groupingBy(RoomItemVO::getRoomCategoryId, LinkedHashMap::new, Collectors.toList()));
        for (RoomCategoryRoomsGroupVO group : groups) {
            group.setRooms(roomItemsByCategoryId.getOrDefault(group.getRoomCategoryId(), List.of()));
        }
        return groups;
    }

    private OrderLedgerRecordVO toRecord(OrderLedgerQueryRowVO row) {
        OrderLedgerRecordVO record = new OrderLedgerRecordVO();
        record.setId(defaultString(row.getLedgerEntryId()));
        record.setPoiId(defaultString(row.getPoiId()));
        record.setTypeLabel(entryTypeLabel(row.getEntryType()));
        record.setSourceLabel(sourceLabel(row.getSourceType()));
        record.setOrderId(defaultString(row.getOrderId(), "-"));
        record.setProjectLabel(defaultString(row.getPaymentTypeName(), "-"));
        record.setAmount(toAmount(row.getAmountCent()));
        record.setDebtAmount(zeroAmount());
        record.setPaymentWayLabel(defaultString(row.getPaymentWayName(), "-"));
        record.setPaymentNo("LEDGER-" + defaultString(row.getLedgerEntryId()));
        record.setPaymentTime(formatDateTime(row.getOccurredAt()));
        record.setCreatedAt(formatDateTime(row.getOccurredAt()));
        record.setRoomLabel(roomLabel(row));
        record.setRemark(defaultString(row.getRemark(), "-"));
        record.setOperatorName(defaultString(row.getOperatorName(), "-"));
        record.setDetail(toDetail(row, record));
        return record;
    }

    private OrderLedgerDetailVO toDetail(OrderLedgerQueryRowVO row, OrderLedgerRecordVO record) {
        OrderLedgerDetailVO detail = new OrderLedgerDetailVO();
        detail.setChannelName(record.getSourceLabel());
        detail.setChannelOrderNo(defaultString(row.getOutOrderNo(), defaultString(row.getOrderNo(), record.getPaymentNo())));
        detail.setRoomLabel(record.getRoomLabel());
        detail.setStatusLabel(orderStatusLabel(row.getOrderStatus()));
        detail.setTotalAmount(record.getAmount());
        detail.setStayRange(stayRange(row));
        detail.setGuestSummary(guestSummary(row));
        detail.setProductName(defaultString(row.getRoomCategoryName(), record.getProjectLabel()));
        detail.setBreakdownTitle(record.getProjectLabel());
        detail.setBreakdownAmount(record.getAmount());
        detail.setTotalIncome("income".equals(row.getEntryType()) ? record.getAmount() : zeroAmount());
        detail.setRoomBreakdown(List.of(roomBreakdown(row, record)));
        detail.setExtraLines(List.of(extraLine("备注", record.getRemark(), "操作人：" + record.getOperatorName())));
        detail.setPaymentRecords(List.of(paymentRecord(row, record)));
        return detail;
    }

    private OrderLedgerPaymentRecordVO paymentRecord(OrderLedgerQueryRowVO row, OrderLedgerRecordVO record) {
        OrderLedgerPaymentRecordVO paymentRecord = new OrderLedgerPaymentRecordVO();
        paymentRecord.setId(record.getId());
        paymentRecord.setTypeLabel(record.getTypeLabel());
        paymentRecord.setRoomLabel(record.getRoomLabel());
        paymentRecord.setProjectLabel(record.getProjectLabel());
        paymentRecord.setPaymentWayLabel(record.getPaymentWayLabel());
        paymentRecord.setAmount(record.getAmount());
        paymentRecord.setPaymentNo(record.getPaymentNo());
        paymentRecord.setPaidAt(formatDateTime(row.getOccurredAt()));
        paymentRecord.setRemark(record.getRemark());
        paymentRecord.setActionLabel("查看凭证");
        return paymentRecord;
    }

    private OrderLedgerRoomBreakdownVO roomBreakdown(OrderLedgerQueryRowVO row, OrderLedgerRecordVO record) {
        OrderLedgerRoomBreakdownVO breakdown = new OrderLedgerRoomBreakdownVO();
        breakdown.setDate(row.getOccurredAt() == null ? "" : row.getOccurredAt().toLocalDate().format(DATE_FORMATTER));
        breakdown.setRoomLabel(record.getRoomLabel());
        breakdown.setAmount(record.getAmount());
        return breakdown;
    }

    private OrderLedgerExtraLineVO extraLine(String title, String primary, String secondary) {
        OrderLedgerExtraLineVO line = new OrderLedgerExtraLineVO();
        line.setTitle(title);
        line.setPrimary(primary);
        line.setSecondary(secondary);
        return line;
    }

    private OrderLedgerSummaryVO toSummary(OrderLedgerSummaryQueryVO query) {
        BigDecimal totalIncome = toAmount(query == null ? null : query.getTotalIncomeCent());
        BigDecimal totalExpense = toAmount(query == null ? null : query.getTotalExpenseCent());
        OrderLedgerSummaryVO summary = new OrderLedgerSummaryVO();
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpense(totalExpense);
        summary.setNetIncome(totalIncome.subtract(totalExpense).setScale(2, RoundingMode.HALF_UP));
        return summary;
    }

    private OrderLedgerPaginationVO toPagination(int pageNum, int pageSize, long total) {
        OrderLedgerPaginationVO pagination = new OrderLedgerPaginationVO();
        pagination.setPage(pageNum);
        pagination.setPageSize(pageSize);
        pagination.setTotal(total);
        return pagination;
    }

    private List<OrderLedgerOptionVO> typeOptions() {
        return List.of(option("all", "全部类型"), option("income", "收入"), option("expense", "支出"));
    }

    private List<OrderLedgerOptionVO> sourceOptions() {
        return List.of(option("all", "全部来源"), option("stayOrder", "住宿订单"), option("manualEntry", "记一笔"));
    }

    private OrderLedgerOptionVO option(String value, String label) {
        OrderLedgerOptionVO option = new OrderLedgerOptionVO();
        option.setValue(value);
        option.setLabel(label);
        return option;
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "当前用户上下文不存在");
        }
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!requestedCampId.equals(bundle.getCampId())) {
            throw new BusinessException(40301, "无权访问当前门店收支明细数据");
        }
        return requestedCampId;
    }

    private LocalDateTime parseDateStart(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value).atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw new BusinessException(40001, "beginTime格式错误");
        }
    }

    private LocalDateTime parseDateEndExclusive(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value).plusDays(1).atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw new BusinessException(40001, "endTime格式错误");
        }
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private Integer normalizeIncome(Integer isIncome) {
        return isIncome != null && (isIncome == 0 || isIncome == 1) ? isIncome : null;
    }

    private Integer normalizeType(Integer type) {
        return type != null && (type == 1 || type == 2) ? type : null;
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private List<Long> parseLongList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .map(this::parseNullableLong)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private Long parseNullableLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String entryTypeLabel(String entryType) {
        return "expense".equals(entryType) ? "支出" : "收入";
    }

    private String sourceLabel(String sourceType) {
        return "order".equals(sourceType) ? "住宿订单" : "记一笔";
    }

    private String orderStatusLabel(String status) {
        if (status == null || status.isBlank()) {
            return "-";
        }
        return switch (status) {
            case "booked" -> "已预订";
            case "checked_in" -> "已入住";
            case "completed" -> "已完成";
            case "cancelled" -> "已取消";
            case "refunded" -> "已退款";
            default -> status;
        };
    }

    private String roomLabel(OrderLedgerQueryRowVO row) {
        String categoryName = defaultString(row.getRoomCategoryName(), "-");
        String roomName = defaultString(row.getRoomName(), "-");
        return categoryName + "-" + roomName;
    }

    private String stayRange(OrderLedgerQueryRowVO row) {
        if (row.getStartAt() == null || row.getEndAt() == null) {
            return "-";
        }
        return row.getStartAt().toLocalDate().format(DATE_FORMATTER) + " 至 " + row.getEndAt().toLocalDate().format(DATE_FORMATTER);
    }

    private String guestSummary(OrderLedgerQueryRowVO row) {
        String guestName = defaultString(row.getGuestName(), "-");
        String mobile = defaultString(row.getGuestMobile(), "");
        return mobile.isBlank() ? guestName : guestName + " / " + mobile;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME_FORMATTER);
    }

    private BigDecimal toAmount(Long centValue) {
        if (centValue == null) {
            return zeroAmount();
        }
        return BigDecimal.valueOf(centValue, 2).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroAmount() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
