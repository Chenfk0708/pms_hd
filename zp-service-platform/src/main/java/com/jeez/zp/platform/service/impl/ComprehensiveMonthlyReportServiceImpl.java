package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.ComprehensiveMonthlyReportMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.ComprehensiveMonthlyReportService;
import com.jeez.zp.platform.vo.ComprehensiveMonthlyReportItemVO;
import com.jeez.zp.platform.vo.ComprehensiveMonthlyReportPageResponseVO;
import com.jeez.zp.platform.vo.ComprehensiveMonthlyReportQueryRowVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class ComprehensiveMonthlyReportServiceImpl implements ComprehensiveMonthlyReportService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final String DEFAULT_USER_NAME = "系统自动";

    private final ComprehensiveMonthlyReportMapper comprehensiveMonthlyReportMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public ComprehensiveMonthlyReportPageResponseVO getPage(
            Long campId,
            Long userId,
            String startDate,
            String endDate,
            Integer page,
            Integer pageNum,
            Integer pageSize,
            Integer current
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        LocalDate rangeStart = parseRequiredDate(startDate, "开始日期不能为空");
        LocalDate rangeEnd = parseRequiredDate(endDate, "结束日期不能为空");
        if (rangeEnd.isBefore(rangeStart)) {
            throw new BusinessException(40001, "结束日期不能早于开始日期");
        }

        int resolvedPageNum = normalizePageNum(page, pageNum, current);
        int resolvedPageSize = normalizePageSize(pageSize);
        int roomCount = resolveRoomInventoryCount(resolvedCampId);

        List<ComprehensiveMonthlyReportQueryRowVO> rows = comprehensiveMonthlyReportMapper.selectOrderRows(
                resolvedCampId,
                rangeStart.atStartOfDay(),
                rangeEnd.plusDays(1).atStartOfDay()
        );

        Map<YearMonth, MonthlyAccumulator> summaryByMonth = new TreeMap<>(Comparator.reverseOrder());
        for (ComprehensiveMonthlyReportQueryRowVO row : rows) {
            accumulateRow(summaryByMonth, row, rangeStart, rangeEnd, roomCount);
        }

        List<ComprehensiveMonthlyReportItemVO> items = summaryByMonth.values().stream()
                .map(this::toItem)
                .toList();

        int fromIndex = Math.min((resolvedPageNum - 1) * resolvedPageSize, items.size());
        int toIndex = Math.min(fromIndex + resolvedPageSize, items.size());

        ComprehensiveMonthlyReportPageResponseVO response = new ComprehensiveMonthlyReportPageResponseVO();
        response.setTotal((long) items.size());
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setPageNum(resolvedPageNum);
        response.setList(new ArrayList<>(items.subList(fromIndex, toIndex)));
        return response;
    }

    private void accumulateRow(
            Map<YearMonth, MonthlyAccumulator> summaryByMonth,
            ComprehensiveMonthlyReportQueryRowVO row,
            LocalDate rangeStart,
            LocalDate rangeEnd,
            int roomCount
    ) {
        if (row.getStartAt() == null || row.getEndAt() == null) {
            return;
        }

        LocalDate orderStart = row.getStartAt().toLocalDate();
        LocalDate orderEndExclusive = row.getEndAt().toLocalDate();
        if (!orderEndExclusive.isAfter(orderStart)) {
            orderEndExclusive = orderStart.plusDays(resolvePositiveDayNum(row.getDayNum()));
        }

        LocalDate effectiveStart = max(orderStart, rangeStart);
        LocalDate effectiveEndExclusive = min(orderEndExclusive, rangeEnd.plusDays(1));
        if (!effectiveEndExclusive.isAfter(effectiveStart)) {
            return;
        }

        int totalNights = Math.max(resolvePositiveDayNum(row.getDayNum()), (int) ChronoUnit.DAYS.between(orderStart, orderEndExclusive));
        BigDecimal grossIncome = toAmount(defaultLong(row.getTotalPriceCent()) + defaultLong(row.getCommissionPriceCent()));
        BigDecimal businessIncome = toAmount(defaultLong(row.getBusinessIncomeCent()));

        LocalDate cursor = effectiveStart;
        while (cursor.isBefore(effectiveEndExclusive)) {
            YearMonth month = YearMonth.from(cursor);
            LocalDate monthEndExclusive = month.plusMonths(1).atDay(1);
            LocalDate segmentEndExclusive = min(effectiveEndExclusive, monthEndExclusive);
            int overlapNights = (int) ChronoUnit.DAYS.between(cursor, segmentEndExclusive);
            if (overlapNights > 0) {
                MonthlyAccumulator accumulator = summaryByMonth.computeIfAbsent(
                        month,
                        key -> MonthlyAccumulator.create(key, rangeStart, rangeEnd, roomCount)
                );
                BigDecimal ratio = BigDecimal.valueOf(overlapNights)
                        .divide(BigDecimal.valueOf(totalNights), 10, RoundingMode.HALF_UP);
                accumulator.includeCommissionRoomPrice = accumulator.includeCommissionRoomPrice.add(grossIncome.multiply(ratio));
                accumulator.businessIncome = accumulator.businessIncome.add(businessIncome.multiply(ratio));
                accumulator.openRoomCount += overlapNights;
                accumulator.refreshCreator(row.getCreatedAt(), row.getUserId(), defaultString(row.getUserName(), DEFAULT_USER_NAME));
            }
            cursor = segmentEndExclusive;
        }
    }

    private ComprehensiveMonthlyReportItemVO toItem(MonthlyAccumulator accumulator) {
        BigDecimal inventory = BigDecimal.valueOf(accumulator.inventory);
        BigDecimal soldRoomNights = BigDecimal.valueOf(accumulator.openRoomCount);
        BigDecimal roundedBusinessIncome = scale(accumulator.businessIncome);
        BigDecimal occ = accumulator.inventory == 0
                ? BigDecimal.ZERO
                : soldRoomNights.multiply(ONE_HUNDRED).divide(inventory, 2, RoundingMode.HALF_UP);
        BigDecimal adr = accumulator.openRoomCount == 0
                ? BigDecimal.ZERO
                : roundedBusinessIncome.divide(soldRoomNights, 2, RoundingMode.HALF_UP);
        BigDecimal revPar = accumulator.inventory == 0
                ? BigDecimal.ZERO
                : roundedBusinessIncome.divide(inventory, 2, RoundingMode.HALF_UP);

        ComprehensiveMonthlyReportItemVO item = new ComprehensiveMonthlyReportItemVO();
        item.setDate(accumulator.month.getYear() + "年" + accumulator.month.getMonthValue() + "月");
        item.setStartDate(toEpochMillis(accumulator.monthStart));
        item.setEndDate(toEpochMillis(accumulator.monthEnd));
        item.setIncludeCommissionRoomPrice(scale(accumulator.includeCommissionRoomPrice));
        item.setOrderOtherExpense(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        item.setWriteDownIncome(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        item.setBusinessIncome(roundedBusinessIncome);
        item.setOcc(occ);
        item.setAdr(adr);
        item.setRevPar(revPar);
        item.setCreateTime(toEpochMillis(defaultDateTime(accumulator.latestCreateTime, accumulator.monthEnd.atTime(9, 0))));
        item.setUserId(accumulator.latestUserId);
        item.setUserName(defaultString(accumulator.latestUserName, DEFAULT_USER_NAME));
        item.setInventory(accumulator.inventory);
        item.setOpenRoomCount(accumulator.openRoomCount);
        return item;
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
            throw new BusinessException(40301, "无权访问当前门店综合月报");
        }
        return requestedCampId;
    }

    private int resolveRoomInventoryCount(Long campId) {
        Integer roomCount = comprehensiveMonthlyReportMapper.countActiveRooms(campId);
        if (roomCount != null && roomCount > 0) {
            return roomCount;
        }
        return Optional.ofNullable(comprehensiveMonthlyReportMapper.sumRoomCategoryCount(campId)).orElse(0);
    }

    private LocalDate parseRequiredDate(String value, String errorMessage) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new BusinessException(40001, errorMessage);
        }
        return LocalDate.parse(trimmed.substring(0, 10));
    }

    private int normalizePageNum(Integer page, Integer pageNum, Integer current) {
        Integer candidate = page != null ? page : (pageNum != null ? pageNum : current);
        return candidate == null || candidate < 1 ? DEFAULT_PAGE_NUM : candidate;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private int resolvePositiveDayNum(Integer dayNum) {
        return dayNum == null || dayNum < 1 ? 1 : dayNum;
    }

    private BigDecimal toAmount(long amountCent) {
        return BigDecimal.valueOf(amountCent, 2);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private LocalDate max(LocalDate first, LocalDate second) {
        return first.isAfter(second) ? first : second;
    }

    private LocalDate min(LocalDate first, LocalDate second) {
        return first.isBefore(second) ? first : second;
    }

    private LocalDateTime defaultDateTime(LocalDateTime value, LocalDateTime fallback) {
        return value == null ? fallback : value;
    }

    private Long toEpochMillis(LocalDate value) {
        return value.atStartOfDay(SHANGHAI_ZONE).toInstant().toEpochMilli();
    }

    private Long toEpochMillis(LocalDateTime value) {
        return value.atZone(SHANGHAI_ZONE).toInstant().toEpochMilli();
    }

    private static final class MonthlyAccumulator {

        private final YearMonth month;
        private final LocalDate monthStart;
        private final LocalDate monthEnd;
        private final int inventory;
        private BigDecimal includeCommissionRoomPrice = BigDecimal.ZERO;
        private BigDecimal businessIncome = BigDecimal.ZERO;
        private int openRoomCount = 0;
        private LocalDateTime latestCreateTime;
        private String latestUserId;
        private String latestUserName;

        private MonthlyAccumulator(YearMonth month, LocalDate monthStart, LocalDate monthEnd, int inventory) {
            this.month = month;
            this.monthStart = monthStart;
            this.monthEnd = monthEnd;
            this.inventory = inventory;
        }

        private static MonthlyAccumulator create(YearMonth month, LocalDate rangeStart, LocalDate rangeEnd, int roomCount) {
            LocalDate monthStart = rangeStart.isAfter(month.atDay(1)) ? rangeStart : month.atDay(1);
            LocalDate monthEnd = rangeEnd.isBefore(month.atEndOfMonth()) ? rangeEnd : month.atEndOfMonth();
            int inventory = monthEnd.isBefore(monthStart)
                    ? 0
                    : (int) ChronoUnit.DAYS.between(monthStart, monthEnd.plusDays(1)) * roomCount;
            return new MonthlyAccumulator(month, monthStart, monthEnd, inventory);
        }

        private void refreshCreator(LocalDateTime createdAt, String userId, String userName) {
            if (latestCreateTime == null || (createdAt != null && createdAt.isAfter(latestCreateTime))) {
                latestCreateTime = createdAt;
                latestUserId = userId;
                latestUserName = userName;
            }
        }
    }
}
