package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.ProfitReportMapper;
import com.jeez.zp.platform.service.ProfitReportService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.ProfitReportCleanTaskRowVO;
import com.jeez.zp.platform.vo.ProfitReportExportResponseVO;
import com.jeez.zp.platform.vo.ProfitReportOrderRowVO;
import com.jeez.zp.platform.vo.ProfitReportPageResponseVO;
import com.jeez.zp.platform.vo.ProfitReportRowVO;
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
public class ProfitReportServiceImpl implements ProfitReportService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final BigDecimal ZERO_AMOUNT = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final long NIGHT_CLEAN_COST_CENT = 3600L;
    private static final long DAILY_CLEAN_COST_CENT = 6100L;
    private static final long CHECKOUT_CLEAN_COST_CENT = 6600L;
    private static final long OTHER_CLEAN_COST_CENT = 7600L;

    private final ProfitReportMapper profitReportMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public ProfitReportPageResponseVO getProfitReport(
            Long campId,
            Long userId,
            String startDate,
            String endDate,
            Long poiId,
            Long roomCategoryId,
            Long roomCategoryGroupId,
            Long channelId,
            Long roomId,
            Integer isCleanCost,
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

        int resolvedPageNum = normalizePageNum(pageNum, current);
        int resolvedPageSize = normalizePageSize(pageSize);
        LocalDateTime rangeStartTime = rangeStart.atStartOfDay();
        LocalDateTime rangeEndExclusive = rangeEnd.plusDays(1).atStartOfDay();

        List<ProfitReportOrderRowVO> orderRows = profitReportMapper.selectOrderRows(
                resolvedCampId,
                rangeStartTime,
                rangeEndExclusive,
                poiId,
                roomCategoryId,
                roomCategoryGroupId,
                channelId,
                roomId
        );

        List<ProfitReportCleanTaskRowVO> cleanTaskRows = shouldIncludeCleanCost(isCleanCost)
                ? profitReportMapper.selectCleanTaskRows(
                        resolvedCampId,
                        rangeStartTime,
                        rangeEndExclusive,
                        poiId,
                        roomCategoryId,
                        roomCategoryGroupId,
                        roomId
                )
                : List.of();

        Map<LocalDate, MutableProfitRow> aggregated = aggregate(rangeStart, rangeEnd, orderRows, cleanTaskRows, shouldIncludeCleanCost(isCleanCost));
        List<ProfitReportRowVO> rows = buildRows(aggregated);
        PageSlice<ProfitReportRowVO> pageSlice = slice(rows, resolvedPageNum, resolvedPageSize);

        ProfitReportPageResponseVO response = new ProfitReportPageResponseVO();
        response.setTotal(pageSlice.total());
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setPageNum(resolvedPageNum);
        response.setPageSize(resolvedPageSize);
        response.setPages(resolvePages(pageSlice.total(), resolvedPageSize));
        response.setList(pageSlice.items());
        return response;
    }

    @Override
    public ProfitReportExportResponseVO exportProfitReport(
            Long campId,
            Long userId,
            String startDate,
            String endDate,
            Long poiId,
            Long roomCategoryId,
            Long roomCategoryGroupId,
            Long channelId,
            Long roomId,
            Integer isCleanCost
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        LocalDate rangeStart = parseRequiredDate(startDate, "startDate");
        LocalDate rangeEnd = parseRequiredDate(endDate, "endDate");
        ProfitReportPageResponseVO page = getProfitReport(
                resolvedCampId,
                userId,
                rangeStart.toString(),
                rangeEnd.toString(),
                poiId,
                roomCategoryId,
                roomCategoryGroupId,
                channelId,
                roomId,
                isCleanCost,
                1,
                1,
                Integer.MAX_VALUE
        );
        String startToken = rangeStart.toString().replace("-", "");
        String endToken = rangeEnd.toString().replace("-", "");

        ProfitReportExportResponseVO response = new ProfitReportExportResponseVO();
        response.setTaskId("PROFIT-REPORT-EXPORT-" + resolvedCampId + "-" + startToken + "-" + endToken);
        response.setFileName("profit_report_" + startToken + "_" + endToken + ".csv");
        response.setContentType("text/csv");
        response.setDownloadUrl(buildDownloadUrl(
                resolvedCampId,
                rangeStart,
                rangeEnd,
                poiId,
                roomCategoryId,
                roomCategoryGroupId,
                channelId,
                roomId,
                isCleanCost
        ));
        response.setTotal(Math.toIntExact(page.getTotal()));
        response.setRows(page.getList());
        return response;
    }

    private String buildDownloadUrl(
            Long campId,
            LocalDate rangeStart,
            LocalDate rangeEnd,
            Long poiId,
            Long roomCategoryId,
            Long roomCategoryGroupId,
            Long channelId,
            Long roomId,
            Integer isCleanCost
    ) {
        StringBuilder url = new StringBuilder("/api/statistics/profit-report/export/download")
                .append("?campId=").append(campId)
                .append("&startDate=").append(rangeStart)
                .append("&endDate=").append(rangeEnd)
                .append("&isCleanCost=").append(shouldIncludeCleanCost(isCleanCost) ? 1 : 0);
        appendQueryParam(url, "poiId", poiId);
        appendQueryParam(url, "roomCategoryId", roomCategoryId);
        appendQueryParam(url, "roomCategoryGroupId", roomCategoryGroupId);
        appendQueryParam(url, "channelId", channelId);
        appendQueryParam(url, "roomId", roomId);
        return url.toString();
    }

    private void appendQueryParam(StringBuilder url, String name, Long value) {
        if (value != null) {
            url.append("&").append(name).append("=").append(value);
        }
    }

    private Map<LocalDate, MutableProfitRow> aggregate(
            LocalDate rangeStart,
            LocalDate rangeEnd,
            List<ProfitReportOrderRowVO> orderRows,
            List<ProfitReportCleanTaskRowVO> cleanTaskRows,
            boolean includeCleanCost
    ) {
        Map<LocalDate, MutableProfitRow> aggregated = new LinkedHashMap<>();

        for (ProfitReportOrderRowVO row : orderRows) {
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

            int totalNights = Math.max(resolvePositiveDayNum(row.getDayNum()), (int) ChronoUnit.DAYS.between(orderStart, orderEndExclusive));
            List<Long> nightlyShares = allocateEvenly(defaultLong(row.getBusinessIncomeCent()), totalNights);

            LocalDate cursor = effectiveStart;
            while (cursor.isBefore(effectiveEndExclusive)) {
                int shareIndex = (int) ChronoUnit.DAYS.between(orderStart, cursor);
                aggregated.computeIfAbsent(cursor, MutableProfitRow::new)
                        .addRoomFeeMinusCommission(nightlyShares.get(Math.max(0, Math.min(shareIndex, nightlyShares.size() - 1))));
                cursor = cursor.plusDays(1);
            }
        }

        if (includeCleanCost) {
            for (ProfitReportCleanTaskRowVO row : cleanTaskRows) {
                if (!"DONE".equalsIgnoreCase(trimToNull(row.getTaskStatus())) || row.getDeadlineAt() == null) {
                    continue;
                }
                LocalDate cleanDate = row.getDeadlineAt().toLocalDate();
                if (cleanDate.isBefore(rangeStart) || cleanDate.isAfter(rangeEnd)) {
                    continue;
                }
                aggregated.computeIfAbsent(cleanDate, MutableProfitRow::new)
                        .addCleanCost(resolveCleanCostCent(row.getTaskType()));
            }
        }

        if (!aggregated.isEmpty()) {
            LocalDate cursor = rangeStart;
            while (!cursor.isAfter(rangeEnd)) {
                aggregated.computeIfAbsent(cursor, MutableProfitRow::new);
                cursor = cursor.plusDays(1);
            }
        }
        return aggregated;
    }

    private List<ProfitReportRowVO> buildRows(Map<LocalDate, MutableProfitRow> aggregated) {
        if (aggregated.isEmpty()) {
            return List.of();
        }

        List<ProfitReportRowVO> rows = new ArrayList<>();
        MutableProfitRow total = new MutableProfitRow(null);
        aggregated.values().forEach(total::merge);
        rows.add(total.toView("合计", true));
        aggregated.forEach((date, row) -> rows.add(row.toView(date.toString(), false)));
        return rows;
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
            throw new BusinessException(40301, "no access to current camp profit report");
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

    private boolean shouldIncludeCleanCost(Integer isCleanCost) {
        return isCleanCost != null && isCleanCost == 1;
    }

    private long resolveCleanCostCent(String taskType) {
        String normalized = trimToNull(taskType);
        if ("night_clean".equalsIgnoreCase(normalized)) {
            return NIGHT_CLEAN_COST_CENT;
        }
        if ("daily_clean".equalsIgnoreCase(normalized)) {
            return DAILY_CLEAN_COST_CENT;
        }
        if ("checkout_clean".equalsIgnoreCase(normalized)) {
            return CHECKOUT_CLEAN_COST_CENT;
        }
        return OTHER_CLEAN_COST_CENT;
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

    private BigDecimal toAmount(long cent) {
        return BigDecimal.valueOf(cent, 2).setScale(2, RoundingMode.HALF_UP);
    }

    private String toRate(BigDecimal profitPrice, BigDecimal totalIncome) {
        if (totalIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return "0.00%";
        }
        return profitPrice
                .divide(totalIncome, 6, RoundingMode.HALF_UP)
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

    private final class MutableProfitRow {

        private long roomFeeMinusCommissionCent;
        private long cleanCostCent;

        private MutableProfitRow(LocalDate ignored) {
        }

        private void addRoomFeeMinusCommission(long cent) {
            roomFeeMinusCommissionCent += cent;
        }

        private void addCleanCost(long cent) {
            cleanCostCent += cent;
        }

        private void merge(MutableProfitRow row) {
            roomFeeMinusCommissionCent += row.roomFeeMinusCommissionCent;
            cleanCostCent += row.cleanCostCent;
        }

        private ProfitReportRowVO toView(String date, boolean totalRow) {
            BigDecimal roomFeeMinusCommission = toAmount(roomFeeMinusCommissionCent);
            BigDecimal totalIncome = roomFeeMinusCommission;
            BigDecimal cleanCost = toAmount(cleanCostCent);
            BigDecimal profitPrice = totalIncome.subtract(cleanCost).setScale(2, RoundingMode.HALF_UP);

            ProfitReportRowVO row = new ProfitReportRowVO();
            row.setDate(date);
            row.setIsTotal(totalRow ? 1 : 0);
            row.setRoomFeeMinusCommission(roomFeeMinusCommission);
            row.setTicketPrice(ZERO_AMOUNT);
            row.setCateringPrice(ZERO_AMOUNT);
            row.setOtherOrderExpense(ZERO_AMOUNT);
            row.setWriteDownIncome(ZERO_AMOUNT);
            row.setTotalIncome(totalIncome);
            row.setWriteDownExpenses(ZERO_AMOUNT);
            row.setCleanCost(cleanCost);
            row.setProfitPrice(profitPrice);
            row.setProfitRate(toRate(profitPrice, totalIncome));
            return row;
        }
    }
}
