package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.IncomeReportMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.IncomeReportService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.IncomeReportOrderRowVO;
import com.jeez.zp.platform.vo.IncomeReportPageResponseVO;
import com.jeez.zp.platform.vo.IncomeReportRowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IncomeReportServiceImpl implements IncomeReportService {

    private static final int QUERY_TYPE_DAY = 1;
    private static final int QUERY_TYPE_MONTH = 2;
    private static final int QUERY_TYPE_STORE = 3;
    private static final int QUERY_TYPE_CHANNEL = 4;
    private static final int QUERY_TYPE_ROOM_TYPE = 5;
    private static final int QUERY_TYPE_ROOM = 6;
    private static final int QUERY_TYPE_CHECKOUT = 7;
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final BigDecimal ZERO_AMOUNT = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final IncomeReportMapper incomeReportMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public IncomeReportPageResponseVO getIncomeReport(
            Long campId,
            Long userId,
            String startDate,
            String endDate,
            Long poiId,
            Long roomCategoryId,
            Long roomCategoryGroupId,
            Long channelId,
            Long roomId,
            Integer queryType,
            Integer pageNum,
            Integer current,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        LocalDate rangeStart = parseRequiredDate(startDate, "startDate");
        LocalDate rangeEnd = parseRequiredDate(endDate, "endDate");
        if (rangeEnd.isBefore(rangeStart)) {
            throw new BusinessException(40001, "endDate cannot be earlier than startDate");
        }

        int resolvedQueryType = normalizeQueryType(queryType);
        int resolvedPageNum = normalizePageNum(pageNum, current);
        int resolvedPageSize = normalizePageSize(pageSize);
        LocalDateTime rangeStartTime = rangeStart.atStartOfDay();
        LocalDateTime rangeEndExclusive = rangeEnd.plusDays(1).atStartOfDay();

        List<IncomeReportOrderRowVO> orderRows = incomeReportMapper.selectOrderRows(
                resolvedCampId,
                rangeStartTime,
                rangeEndExclusive,
                poiId,
                roomCategoryId,
                roomCategoryGroupId,
                channelId,
                roomId
        );

        Map<String, MutableIncomeRow> aggregated = aggregate(rangeStart, rangeEnd, resolvedQueryType, orderRows);
        List<IncomeReportRowVO> rows = buildRows(aggregated, resolvedQueryType);
        PageSlice<IncomeReportRowVO> pageSlice = slice(rows, resolvedPageNum, resolvedPageSize);

        IncomeReportPageResponseVO response = new IncomeReportPageResponseVO();
        response.setTotal(pageSlice.total());
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setPageNum(resolvedPageNum);
        response.setPageSize(resolvedPageSize);
        response.setPages(resolvePages(pageSlice.total(), resolvedPageSize));
        response.setList(pageSlice.items());
        return response;
    }

    private Map<String, MutableIncomeRow> aggregate(
            LocalDate rangeStart,
            LocalDate rangeEnd,
            int queryType,
            List<IncomeReportOrderRowVO> orderRows
    ) {
        Map<String, MutableIncomeRow> aggregated = new LinkedHashMap<>();
        if (orderRows == null || orderRows.isEmpty()) {
            return aggregated;
        }

        if (queryType == QUERY_TYPE_DAY) {
            LocalDate cursor = rangeStart;
            while (!cursor.isAfter(rangeEnd)) {
                String label = cursor.toString();
                aggregated.put(label, new MutableIncomeRow(label, label, false));
                cursor = cursor.plusDays(1);
            }
        }

        for (IncomeReportOrderRowVO row : orderRows) {
            if (row.getStartAt() == null || row.getEndAt() == null) {
                continue;
            }

            LocalDate orderStart = row.getStartAt().toLocalDate();
            LocalDate orderEndExclusive = row.getEndAt().toLocalDate();
            if (!orderEndExclusive.isAfter(orderStart)) {
                orderEndExclusive = orderStart.plusDays(resolvePositiveDayNum(row.getDayNum()));
            }

            LocalDate effectiveStart = max(orderStart, rangeStart);
            LocalDate effectiveEndExclusive = min(orderEndExclusive, rangeEnd.plusDays(1));
            if (!effectiveEndExclusive.isAfter(effectiveStart)) {
                continue;
            }

            int totalNights = Math.max(
                    resolvePositiveDayNum(row.getDayNum()),
                    (int) ChronoUnit.DAYS.between(orderStart, orderEndExclusive)
            );
            List<Long> grossShares = allocateEvenly(resolveRoomFeeIncludingCent(row), totalNights);
            List<Long> commissionShares = allocateEvenly(resolveCommissionCent(row), totalNights);
            List<Long> netShares = allocateEvenly(resolveRoomFeeMinusCommissionCent(row), totalNights);

            LocalDate cursor = effectiveStart;
            while (cursor.isBefore(effectiveEndExclusive)) {
                int shareIndex = (int) ChronoUnit.DAYS.between(orderStart, cursor);
                long grossCent = valueAt(grossShares, shareIndex);
                long commissionCent = valueAt(commissionShares, shareIndex);
                long netCent = valueAt(netShares, shareIndex);

                String key = aggregationKey(queryType, cursor, row);
                String label = aggregationLabel(queryType, cursor, row);
                MutableIncomeRow target = aggregated.computeIfAbsent(key, ignored -> new MutableIncomeRow(key, label, false));
                target.addRoomFeeMinusCommission(netCent);
                target.addChannelCommission(commissionCent);
                target.addRoomFeeIncludingCommission(grossCent);
                if ("hour_room".equalsIgnoreCase(trimToNull(row.getOrderType()))) {
                    target.addHourRoomFeeIncludingCommission(grossCent);
                } else {
                    target.addAllDayRoomFeeIncludingCommission(grossCent);
                }
                cursor = cursor.plusDays(1);
            }
        }

        aggregated.entrySet().removeIf(entry -> !entry.getValue().hasIncome());
        return aggregated;
    }

    private List<IncomeReportRowVO> buildRows(Map<String, MutableIncomeRow> aggregated, int queryType) {
        if (aggregated.isEmpty()) {
            return List.of();
        }

        MutableIncomeRow total = new MutableIncomeRow("total", "\u5408\u8ba1", true);
        aggregated.values().forEach(total::merge);

        List<IncomeReportRowVO> rows = new ArrayList<>();
        rows.add(total.toView(total, queryType));
        aggregated.values().forEach(row -> rows.add(row.toView(total, queryType)));
        return rows;
    }

    private String aggregationKey(int queryType, LocalDate date, IncomeReportOrderRowVO row) {
        return switch (queryType) {
            case QUERY_TYPE_MONTH -> monthLabel(date);
            case QUERY_TYPE_STORE -> nonBlank(row.getPoiName(), "Unmapped store");
            case QUERY_TYPE_CHANNEL -> nonBlank(row.getChannelName(), "Direct/Unknown channel");
            case QUERY_TYPE_ROOM_TYPE -> nonBlank(row.getRoomCategoryName(), "Unmapped room type");
            case QUERY_TYPE_ROOM -> nonBlank(row.getRoomName(), "Unmapped room");
            case QUERY_TYPE_CHECKOUT -> row.getEndAt() == null ? date.toString() : row.getEndAt().toLocalDate().toString();
            default -> date.toString();
        };
    }

    private String aggregationLabel(int queryType, LocalDate date, IncomeReportOrderRowVO row) {
        return aggregationKey(queryType, date, row);
    }

    private String monthLabel(LocalDate date) {
        return "%04d-%02d".formatted(date.getYear(), date.getMonthValue());
    }

    private long resolveRoomFeeIncludingCent(IncomeReportOrderRowVO row) {
        long totalPayPriceCent = defaultLong(row.getTotalPayPriceCent());
        if (totalPayPriceCent > 0L) {
            return totalPayPriceCent;
        }
        return Math.max(defaultLong(row.getTotalPriceCent()), 0L);
    }

    private long resolveCommissionCent(IncomeReportOrderRowVO row) {
        return Math.max(defaultLong(row.getCommissionCent()), 0L);
    }

    private long resolveRoomFeeMinusCommissionCent(IncomeReportOrderRowVO row) {
        long grossCent = resolveRoomFeeIncludingCent(row);
        long commissionCent = resolveCommissionCent(row);
        if (grossCent > 0L) {
            return Math.max(grossCent - commissionCent, 0L);
        }
        long settlementAmountCent = defaultLong(row.getSettlementAmountCent());
        if (settlementAmountCent > 0L) {
            return settlementAmountCent;
        }
        return 0L;
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "current user context not found");
        }
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!requestedCampId.equals(bundle.getCampId())) {
            throw new BusinessException(40301, "no access to current camp income report");
        }
        return requestedCampId;
    }

    private LocalDate parseRequiredDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(40001, fieldName + " is required");
        }
        String normalized = value.trim();
        if (normalized.length() > 10) {
            normalized = normalized.substring(0, 10);
        }
        try {
            return LocalDate.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw new BusinessException(40001, fieldName + " is invalid");
        }
    }

    private int normalizeQueryType(Integer queryType) {
        if (queryType == null) {
            return QUERY_TYPE_DAY;
        }
        return switch (queryType) {
            case QUERY_TYPE_DAY, QUERY_TYPE_MONTH, QUERY_TYPE_STORE, QUERY_TYPE_CHANNEL,
                 QUERY_TYPE_ROOM_TYPE, QUERY_TYPE_ROOM, QUERY_TYPE_CHECKOUT -> queryType;
            default -> QUERY_TYPE_DAY;
        };
    }

    private int normalizePageNum(Integer pageNum, Integer current) {
        Integer candidate = pageNum != null ? pageNum : current;
        return candidate == null || candidate < 1 ? DEFAULT_PAGE_NUM : candidate;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private int resolvePages(long total, int pageSize) {
        return Math.max(1, (int) Math.ceil(total / (double) Math.max(pageSize, 1)));
    }

    private int resolvePositiveDayNum(Integer dayNum) {
        return dayNum == null || dayNum < 1 ? 1 : dayNum;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private List<Long> allocateEvenly(long totalCent, int parts) {
        int safeParts = Math.max(parts, 1);
        List<Long> values = new ArrayList<>(safeParts);
        long base = totalCent / safeParts;
        long remainder = totalCent % safeParts;
        for (int index = 0; index < safeParts; index++) {
            values.add(base + (index < remainder ? 1 : 0));
        }
        return values;
    }

    private long valueAt(List<Long> values, int index) {
        if (values.isEmpty()) {
            return 0L;
        }
        return values.get(Math.max(0, Math.min(index, values.size() - 1)));
    }

    private BigDecimal toAmount(long cent) {
        return BigDecimal.valueOf(cent, 2).setScale(2, RoundingMode.HALF_UP);
    }

    private String toRate(long valueCent, long totalCent) {
        if (totalCent <= 0L) {
            return "0.00%";
        }
        return BigDecimal.valueOf(valueCent)
                .divide(BigDecimal.valueOf(totalCent), 6, RoundingMode.HALF_UP)
                .multiply(HUNDRED)
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString() + "%";
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String nonBlank(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

    private LocalDate max(LocalDate left, LocalDate right) {
        return left.isAfter(right) ? left : right;
    }

    private LocalDate min(LocalDate left, LocalDate right) {
        return left.isBefore(right) ? left : right;
    }

    private <T> PageSlice<T> slice(List<T> items, int pageNum, int pageSize) {
        long total = items.size();
        int fromIndex = Math.min((pageNum - 1) * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return new PageSlice<>(total, items.subList(fromIndex, toIndex));
    }

    private record PageSlice<T>(long total, List<T> items) {
    }

    private final class MutableIncomeRow {

        private final String key;
        private final String label;
        private final boolean totalRow;
        private long roomFeeMinusCommissionCent;
        private long channelCommissionCent;
        private long roomFeeIncludingCommissionCent;
        private long allDayRoomFeeIncludingCommissionCent;
        private long hourRoomFeeIncludingCommissionCent;

        private MutableIncomeRow(String key, String label, boolean totalRow) {
            this.key = key;
            this.label = label;
            this.totalRow = totalRow;
        }

        private void addRoomFeeMinusCommission(long cent) {
            roomFeeMinusCommissionCent += cent;
        }

        private void addChannelCommission(long cent) {
            channelCommissionCent += cent;
        }

        private void addRoomFeeIncludingCommission(long cent) {
            roomFeeIncludingCommissionCent += cent;
        }

        private void addAllDayRoomFeeIncludingCommission(long cent) {
            allDayRoomFeeIncludingCommissionCent += cent;
        }

        private void addHourRoomFeeIncludingCommission(long cent) {
            hourRoomFeeIncludingCommissionCent += cent;
        }

        private void merge(MutableIncomeRow row) {
            roomFeeMinusCommissionCent += row.roomFeeMinusCommissionCent;
            channelCommissionCent += row.channelCommissionCent;
            roomFeeIncludingCommissionCent += row.roomFeeIncludingCommissionCent;
            allDayRoomFeeIncludingCommissionCent += row.allDayRoomFeeIncludingCommissionCent;
            hourRoomFeeIncludingCommissionCent += row.hourRoomFeeIncludingCommissionCent;
        }

        private boolean hasIncome() {
            return roomFeeMinusCommissionCent != 0L
                    || channelCommissionCent != 0L
                    || roomFeeIncludingCommissionCent != 0L
                    || allDayRoomFeeIncludingCommissionCent != 0L
                    || hourRoomFeeIncludingCommissionCent != 0L;
        }

        private IncomeReportRowVO toView(MutableIncomeRow total, int queryType) {
            IncomeReportRowVO row = new IncomeReportRowVO();
            row.setKey(key);
            row.setLabel(label);
            row.setIsTotal(totalRow ? 1 : 0);
            row.setRoomFeeMinusCommission(toAmount(roomFeeMinusCommissionCent));
            row.setChannelCommission(toAmount(channelCommissionCent));
            row.setRoomFeeIncludingCommission(toAmount(roomFeeIncludingCommissionCent));
            row.setAllDayRoomFeeIncludingCommission(toAmount(allDayRoomFeeIncludingCommissionCent));
            row.setHourRoomFeeIncludingCommission(toAmount(hourRoomFeeIncludingCommissionCent));
            row.setOtherExpense(ZERO_AMOUNT);
            row.setAccommodationExpense(ZERO_AMOUNT);
            row.setCateringExpense(ZERO_AMOUNT);
            row.setSupermarketExpense(ZERO_AMOUNT);
            row.setEntertainmentExpense(ZERO_AMOUNT);
            row.setVenueExpense(ZERO_AMOUNT);
            row.setOrderTotalIncome(toAmount(roomFeeIncludingCommissionCent));
            row.setManualIncome(ZERO_AMOUNT);
            row.setManualAccommodationIncome(ZERO_AMOUNT);
            row.setManualCateringIncome(ZERO_AMOUNT);
            row.setManualSupermarketIncome(ZERO_AMOUNT);
            row.setManualEntertainmentIncome(ZERO_AMOUNT);
            row.setManualVenueIncome(ZERO_AMOUNT);
            row.setBusinessIncomeIncludingCommission(toAmount(roomFeeIncludingCommissionCent));
            row.setBusinessIncomeMinusCommission(toAmount(roomFeeMinusCommissionCent));
            if (queryType == QUERY_TYPE_CHANNEL) {
                row.setRoomFeeMinusCommissionRatio(toRate(roomFeeMinusCommissionCent, total.roomFeeMinusCommissionCent));
                row.setChannelCommissionRatio(toRate(channelCommissionCent, total.channelCommissionCent));
            }
            row.setDetailContext(totalRow ? "\u5168\u90e8\u4f4f\u5bbf\u6536\u5165\u6c47\u603b" : label + "\u4f4f\u5bbf\u6536\u5165\u6c47\u603b");
            return row;
        }
    }
}
