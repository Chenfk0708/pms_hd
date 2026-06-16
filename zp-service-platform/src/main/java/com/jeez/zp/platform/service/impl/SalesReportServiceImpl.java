package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.SalesReportMapper;
import com.jeez.zp.platform.service.SalesReportService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.SalesReportOrderRowVO;
import com.jeez.zp.platform.vo.SalesReportPageResponseVO;
import com.jeez.zp.platform.vo.SalesReportRowVO;
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
public class SalesReportServiceImpl implements SalesReportService {

    private static final int QUERY_TYPE_DAY = 1;
    private static final int QUERY_TYPE_MONTH = 2;
    private static final int QUERY_TYPE_STORE = 3;
    private static final int QUERY_TYPE_CHANNEL = 4;
    private static final int QUERY_TYPE_ROOM_TYPE = 5;
    private static final int QUERY_TYPE_ROOM = 6;
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final SalesReportMapper salesReportMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public SalesReportPageResponseVO getSalesReport(
            Long campId,
            Long userId,
            String startDate,
            String endDate,
            List<Long> poiIds,
            List<Long> roomCategoryIds,
            List<Long> roomCategoryGroupIds,
            List<Long> channelIds,
            List<Long> roomIds,
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
        int roomInventory = Math.max(defaultInteger(salesReportMapper.countActiveRooms(resolvedCampId)), 0);
        LocalDateTime rangeStartTime = rangeStart.atStartOfDay();
        LocalDateTime rangeEndExclusive = rangeEnd.plusDays(1).atStartOfDay();

        List<SalesReportOrderRowVO> orderRows = salesReportMapper.selectOrderRows(
                resolvedCampId,
                rangeStartTime,
                rangeEndExclusive,
                emptyToNull(poiIds),
                emptyToNull(roomCategoryIds),
                emptyToNull(roomCategoryGroupIds),
                emptyToNull(channelIds),
                emptyToNull(roomIds)
        );

        Map<String, MutableSalesRow> aggregated = aggregate(
                rangeStart,
                rangeEnd,
                resolvedQueryType,
                orderRows,
                roomInventory
        );
        List<SalesReportRowVO> rows = buildRows(aggregated, resolvedQueryType, roomInventory);
        PageSlice<SalesReportRowVO> pageSlice = slice(rows, resolvedPageNum, resolvedPageSize);

        SalesReportPageResponseVO response = new SalesReportPageResponseVO();
        response.setTotal(pageSlice.total());
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setPageNum(resolvedPageNum);
        response.setPageSize(resolvedPageSize);
        response.setPages(resolvePages(pageSlice.total(), resolvedPageSize));
        response.setList(pageSlice.items());
        return response;
    }

    private Map<String, MutableSalesRow> aggregate(
            LocalDate rangeStart,
            LocalDate rangeEnd,
            int queryType,
            List<SalesReportOrderRowVO> orderRows,
            int roomInventory
    ) {
        Map<String, MutableSalesRow> aggregated = new LinkedHashMap<>();
        if (queryType == QUERY_TYPE_DAY) {
            LocalDate cursor = rangeStart;
            while (!cursor.isAfter(rangeEnd)) {
                String label = cursor.toString();
                aggregated.put(label, new MutableSalesRow(label, label, false, roomInventory));
                cursor = cursor.plusDays(1);
            }
        }

        if (orderRows == null || orderRows.isEmpty()) {
            return Map.of();
        }

        for (SalesReportOrderRowVO row : orderRows) {
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
                String key = aggregationKey(queryType, cursor, row);
                String label = aggregationLabel(queryType, cursor, row);
                MutableSalesRow target = aggregated.computeIfAbsent(
                        key,
                        ignored -> new MutableSalesRow(key, label, false, roomInventory)
                );
                target.addRoomNight(
                        valueAt(grossShares, shareIndex),
                        valueAt(commissionShares, shareIndex),
                        valueAt(netShares, shareIndex),
                        "hour_room".equalsIgnoreCase(trimToNull(row.getOrderType()))
                );
                cursor = cursor.plusDays(1);
            }
        }

        aggregated.entrySet().removeIf(entry -> !entry.getValue().hasSales());
        return aggregated;
    }

    private List<SalesReportRowVO> buildRows(Map<String, MutableSalesRow> aggregated, int queryType, int roomInventory) {
        if (aggregated.isEmpty()) {
            return List.of();
        }

        MutableSalesRow total = new MutableSalesRow("total", "合计", true, totalInventory(aggregated, queryType, roomInventory));
        aggregated.values().forEach(total::merge);

        List<SalesReportRowVO> rows = new ArrayList<>();
        rows.add(total.toView(total, queryType));
        aggregated.values().forEach(row -> rows.add(row.toView(total, queryType)));
        return rows;
    }

    private int totalInventory(Map<String, MutableSalesRow> aggregated, int queryType, int roomInventory) {
        if (queryType == QUERY_TYPE_DAY || queryType == QUERY_TYPE_MONTH) {
            return aggregated.values().stream().mapToInt(row -> row.roomCount).sum();
        }
        return roomInventory;
    }

    private String aggregationKey(int queryType, LocalDate date, SalesReportOrderRowVO row) {
        return switch (queryType) {
            case QUERY_TYPE_MONTH -> monthLabel(date);
            case QUERY_TYPE_STORE -> nonBlank(row.getPoiName(), "未关联门店");
            case QUERY_TYPE_CHANNEL -> nonBlank(row.getChannelName(), "自来客");
            case QUERY_TYPE_ROOM_TYPE -> nonBlank(row.getRoomCategoryName(), "未关联房型");
            case QUERY_TYPE_ROOM -> nonBlank(row.getRoomName(), "未关联房间");
            default -> date.toString();
        };
    }

    private String aggregationLabel(int queryType, LocalDate date, SalesReportOrderRowVO row) {
        return aggregationKey(queryType, date, row);
    }

    private String monthLabel(LocalDate date) {
        return "%04d-%02d".formatted(date.getYear(), date.getMonthValue());
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
            throw new BusinessException(40301, "no access to current camp sales report");
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
                 QUERY_TYPE_ROOM_TYPE, QUERY_TYPE_ROOM -> queryType;
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

    private long resolveRoomFeeIncludingCent(SalesReportOrderRowVO row) {
        long totalPayPriceCent = defaultLong(row.getTotalPayPriceCent());
        if (totalPayPriceCent > 0L) {
            return totalPayPriceCent;
        }
        return Math.max(defaultLong(row.getTotalPriceCent()), 0L);
    }

    private long resolveCommissionCent(SalesReportOrderRowVO row) {
        return Math.max(defaultLong(row.getCommissionCent()), 0L);
    }

    private long resolveRoomFeeMinusCommissionCent(SalesReportOrderRowVO row) {
        long grossCent = resolveRoomFeeIncludingCent(row);
        long commissionCent = resolveCommissionCent(row);
        if (grossCent > 0L) {
            return Math.max(grossCent - commissionCent, 0L);
        }
        return Math.max(defaultLong(row.getSettlementAmountCent()), 0L);
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private int defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private List<Long> emptyToNull(List<Long> values) {
        return values == null || values.isEmpty() ? null : values;
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

    private String toRate(long value, long total) {
        if (total <= 0L) {
            return "0.00%";
        }
        return BigDecimal.valueOf(value)
                .divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP)
                .multiply(HUNDRED)
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString() + "%";
    }

    private BigDecimal divideAmount(long cent, int count) {
        if (count <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return toAmount(cent).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal revPar(long cent, int roomCount) {
        if (roomCount <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return toAmount(cent).divide(BigDecimal.valueOf(roomCount), 2, RoundingMode.HALF_UP);
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

    private final class MutableSalesRow {

        private final String key;
        private final String label;
        private final boolean totalRow;
        private int roomCount;
        private int openRoomCount;
        private int allDayOpenRoomCount;
        private int hourOpenRoomCount;
        private long roomFeeIncludingCommissionCent;
        private long commissionCent;
        private long roomFeeMinusCommissionCent;

        private MutableSalesRow(String key, String label, boolean totalRow, int roomCount) {
            this.key = key;
            this.label = label;
            this.totalRow = totalRow;
            this.roomCount = roomCount;
        }

        private void addRoomNight(long grossCent, long commissionCent, long netCent, boolean hourRoom) {
            openRoomCount += 1;
            if (hourRoom) {
                hourOpenRoomCount += 1;
            } else {
                allDayOpenRoomCount += 1;
            }
            this.roomFeeIncludingCommissionCent += grossCent;
            this.commissionCent += commissionCent;
            this.roomFeeMinusCommissionCent += netCent;
        }

        private void merge(MutableSalesRow row) {
            openRoomCount += row.openRoomCount;
            allDayOpenRoomCount += row.allDayOpenRoomCount;
            hourOpenRoomCount += row.hourOpenRoomCount;
            roomFeeIncludingCommissionCent += row.roomFeeIncludingCommissionCent;
            commissionCent += row.commissionCent;
            roomFeeMinusCommissionCent += row.roomFeeMinusCommissionCent;
        }

        private boolean hasSales() {
            return openRoomCount > 0 || roomFeeIncludingCommissionCent > 0L || roomFeeMinusCommissionCent > 0L;
        }

        private SalesReportRowVO toView(MutableSalesRow total, int queryType) {
            SalesReportRowVO row = new SalesReportRowVO();
            row.setIsTotal(totalRow ? 1 : 0);
            applyDimension(row, queryType);
            row.setRoomCount(roomCount);
            row.setCanSaleRoomCount(roomCount);
            row.setOpenRoomCount(openRoomCount);
            row.setAllDayOpenRoomCount(allDayOpenRoomCount);
            row.setHourOpenRoomCount(hourOpenRoomCount);
            row.setOcc(toRate(openRoomCount, roomCount));
            row.setAdr(divideAmount(roomFeeIncludingCommissionCent, openRoomCount));
            row.setAdrAfterCommission(divideAmount(roomFeeMinusCommissionCent, openRoomCount));
            row.setRevPar(revPar(roomFeeIncludingCommissionCent, roomCount));
            row.setRevParAfterCommission(revPar(roomFeeMinusCommissionCent, roomCount));
            row.setRoomFeeIncludingCommission(toAmount(roomFeeIncludingCommissionCent));
            row.setCommission(toAmount(commissionCent));
            row.setRoomFeeMinusCommission(toAmount(roomFeeMinusCommissionCent));
            row.setOrderCount(openRoomCount);
            row.setSaleRoomRate(toRate(openRoomCount, total.openRoomCount));
            row.setAllDaySaleRoomRate(toRate(allDayOpenRoomCount, total.allDayOpenRoomCount));
            row.setHourSaleRoomRate(toRate(hourOpenRoomCount, total.hourOpenRoomCount));
            row.setOrderRate(toRate(openRoomCount, total.openRoomCount));
            return row;
        }

        private void applyDimension(SalesReportRowVO row, int queryType) {
            switch (queryType) {
                case QUERY_TYPE_MONTH -> row.setMonth(label);
                case QUERY_TYPE_STORE -> row.setPoiName(label);
                case QUERY_TYPE_CHANNEL -> row.setChannelName(label);
                case QUERY_TYPE_ROOM_TYPE -> row.setRoomCategoryName(label);
                case QUERY_TYPE_ROOM -> row.setRoomName(label);
                default -> row.setDate(label);
            }
        }
    }
}
