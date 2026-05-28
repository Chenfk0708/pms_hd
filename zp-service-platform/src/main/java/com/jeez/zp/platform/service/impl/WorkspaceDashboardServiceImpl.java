package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.WorkspaceDashboardMapper;
import com.jeez.zp.platform.service.WorkspaceDashboardService;
import com.jeez.zp.platform.vo.ChannelVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.WorkspaceAccommodationAnalysisVO;
import com.jeez.zp.platform.vo.WorkspaceBacklogItemVO;
import com.jeez.zp.platform.vo.WorkspaceCampFlowChannelInfoVO;
import com.jeez.zp.platform.vo.WorkspaceCampFlowVO;
import com.jeez.zp.platform.vo.WorkspaceDashboardOrderRowVO;
import com.jeez.zp.platform.vo.WorkspaceDashboardRoomRowVO;
import com.jeez.zp.platform.vo.WorkspaceGrowthTrendItemVO;
import com.jeez.zp.platform.vo.WorkspaceHomePageVO;
import com.jeez.zp.platform.vo.WorkspaceMemoItemVO;
import com.jeez.zp.platform.vo.WorkspaceMemoPageResponseVO;
import com.jeez.zp.platform.vo.WorkspaceOrderItemVO;
import com.jeez.zp.platform.vo.WorkspaceOrderListRowVO;
import com.jeez.zp.platform.vo.WorkspaceOrderOriginItemVO;
import com.jeez.zp.platform.vo.WorkspaceOrdersResponseVO;
import com.jeez.zp.platform.vo.WorkspacePaginationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkspaceDashboardServiceImpl implements WorkspaceDashboardService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final List<String> REPAIR_KEYWORDS = List.of("repair", "maintain", "offline", "维修", "故障");
    private static final String AUTHORIZED_STATUS = "authorized";

    private final WorkspaceDashboardMapper workspaceDashboardMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public WorkspaceHomePageVO getHomePage(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        LocalDate today = LocalDate.now(SHANGHAI_ZONE);
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime nextDayStart = today.plusDays(1).atStartOfDay();

        List<WorkspaceDashboardRoomRowVO> roomRows = workspaceDashboardMapper.selectRoomRows(resolvedCampId, dayStart, nextDayStart);
        List<WorkspaceDashboardOrderRowVO> orderRows = workspaceDashboardMapper.selectOrderRows(resolvedCampId, dayStart, nextDayStart);

        WorkspaceHomePageVO response = new WorkspaceHomePageVO();
        response.setNowPredictCheckIn(count(roomRows, row -> isBooked(row) && isSameDate(row.getCheckInAt(), today)));
        response.setNowAlreadyCheckIn(count(roomRows, this::isCheckedIn));
        response.setNowPredictCheckOut(count(roomRows, row -> isCheckedIn(row) && isSameDate(row.getCheckOutAt(), today)));
        response.setNowOnSaleNum(count(roomRows, this::isOnSale));
        response.setUserBusyRepairNum(count(roomRows, row -> isRepair(row.getLockStatus())));
        response.setDirtyNum(count(roomRows, row -> isDirty(row.getCleanStatus())));
        response.setExceptionOrderNum(Optional.ofNullable(workspaceDashboardMapper.countExceptionOrders(resolvedCampId)).orElse(0));
        response.setNowIncome(sumBusinessIncomeCent(orderRows, today, today));
        return response;
    }

    @Override
    public WorkspaceAccommodationAnalysisVO getAccommodationAnalysis(Long campId, Long userId, String startDate, String endDate) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        LocalDate rangeStart = parseRequiredDate(startDate, "startDate is required");
        LocalDate rangeEnd = parseRequiredDate(endDate, "endDate is required");
        if (rangeEnd.isBefore(rangeStart)) {
            throw new BusinessException(40001, "endDate cannot be earlier than startDate");
        }

        int activeRoomCount = resolveRoomCount(resolvedCampId);
        int inventory = Math.max(0, (int) ChronoUnit.DAYS.between(rangeStart, rangeEnd.plusDays(1))) * activeRoomCount;
        List<WorkspaceDashboardOrderRowVO> orderRows = workspaceDashboardMapper.selectOrderRows(
                resolvedCampId,
                rangeStart.atStartOfDay(),
                rangeEnd.plusDays(1).atStartOfDay()
        );

        Map<LocalDate, DailyAccumulator> dailyAccumulators = new LinkedHashMap<>();
        LocalDate cursor = rangeStart;
        while (!cursor.isAfter(rangeEnd)) {
            dailyAccumulators.put(cursor, new DailyAccumulator(cursor, activeRoomCount));
            cursor = cursor.plusDays(1);
        }

        BigDecimal grossIncome = BigDecimal.ZERO;
        BigDecimal businessIncome = BigDecimal.ZERO;
        int openRoomCount = 0;
        Map<String, Integer> orderOriginCounts = new LinkedHashMap<>();

        for (WorkspaceDashboardOrderRowVO row : orderRows) {
            OverlapAllocation allocation = allocate(row, rangeStart, rangeEnd);
            if (allocation == null) {
                continue;
            }

            grossIncome = grossIncome.add(allocation.grossIncome());
            businessIncome = businessIncome.add(allocation.businessIncome());
            openRoomCount += allocation.openRoomCount();
            orderOriginCounts.merge(resolveChannelName(row), 1, Integer::sum);

            for (Map.Entry<LocalDate, DailyShare> entry : allocation.dailyShares().entrySet()) {
                DailyAccumulator accumulator = dailyAccumulators.get(entry.getKey());
                if (accumulator == null) {
                    continue;
                }
                accumulator.businessIncome = accumulator.businessIncome.add(entry.getValue().businessIncome());
                accumulator.openRoomCount += entry.getValue().openRoomCount();
            }
        }

        WorkspaceAccommodationAnalysisVO response = new WorkspaceAccommodationAnalysisVO();
        response.setBusinessIncome(scale(businessIncome));
        response.setRoomFeePriceIncludingCommission(scale(grossIncome));
        response.setWriteDownIncome(zeroAmount());
        response.setOtherOrderExpense(zeroAmount());
        response.setOpenRoomCount(openRoomCount);
        response.setRoomCount(inventory);
        response.setAllDayOpenRoomCount(openRoomCount);
        response.setHourOpenRoomCount(0);
        response.setOcc(dividePercentage(BigDecimal.valueOf(openRoomCount), inventory));
        response.setAdr(divideAmount(businessIncome, openRoomCount));
        response.setRevPar(divideAmount(businessIncome, inventory));

        List<WorkspaceGrowthTrendItemVO> growthTrendAnalysisList = new ArrayList<>();
        for (DailyAccumulator accumulator : dailyAccumulators.values()) {
            WorkspaceGrowthTrendItemVO item = new WorkspaceGrowthTrendItemVO();
            item.setDate(accumulator.date.toString());
            item.setBusinessIncome(scale(accumulator.businessIncome));
            item.setOcc(dividePercentage(BigDecimal.valueOf(accumulator.openRoomCount), accumulator.inventory));
            item.setAdr(divideAmount(accumulator.businessIncome, accumulator.openRoomCount));
            item.setRevPar(divideAmount(accumulator.businessIncome, accumulator.inventory));
            item.setOpenRoomCount(accumulator.openRoomCount);
            growthTrendAnalysisList.add(item);
        }
        response.setGrowthTrendAnalysisList(growthTrendAnalysisList);

        List<WorkspaceOrderOriginItemVO> orderOriginAnalysisList = orderOriginCounts.entrySet().stream()
                .sorted((left, right) -> {
                    int compareCount = Integer.compare(right.getValue(), left.getValue());
                    if (compareCount != 0) {
                        return compareCount;
                    }
                    return left.getKey().compareTo(right.getKey());
                })
                .map(entry -> {
                    WorkspaceOrderOriginItemVO item = new WorkspaceOrderOriginItemVO();
                    item.setChannelName(entry.getKey());
                    item.setOrderCount(entry.getValue());
                    return item;
                })
                .toList();
        response.setOrderOriginAnalysisList(orderOriginAnalysisList);
        return response;
    }

    @Override
    public WorkspaceCampFlowVO getCampFlow(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        Map<Long, WorkspaceCampFlowChannelInfoVO> deduplicatedChannels = new LinkedHashMap<>();

        for (ChannelVO channel : platformBootstrapMapper.selectChannelsByCampId(resolvedCampId)) {
            if (channel.getChannelId() == null) {
                continue;
            }
            WorkspaceCampFlowChannelInfoVO info = deduplicatedChannels.computeIfAbsent(channel.getChannelId(), key -> {
                WorkspaceCampFlowChannelInfoVO item = new WorkspaceCampFlowChannelInfoVO();
                item.setChannelName(defaultString(channel.getChannelName(), "渠道" + key));
                item.setIsApplyOpen(0);
                return item;
            });
            if (isAuthorized(channel.getStatus())) {
                info.setIsApplyOpen(1);
            }
        }

        WorkspaceCampFlowVO response = new WorkspaceCampFlowVO();
        response.setChannelInfos(new ArrayList<>(deduplicatedChannels.values()));
        response.setIsOpenFlow(response.getChannelInfos().stream().anyMatch(item -> item.getIsApplyOpen() != null && item.getIsApplyOpen() == 1) ? 1 : 0);
        return response;
    }

    @Override
    public WorkspaceOrdersResponseVO getOrders(
            Long campId,
            Long userId,
            String orderType,
            Integer page,
            Integer pageNum,
            Integer current,
            Integer pageSize,
            String keyword
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = normalizePageNum(page, pageNum, current);
        int resolvedPageSize = normalizePageSize(pageSize);
        LocalDate today = LocalDate.now(SHANGHAI_ZONE);

        List<WorkspaceOrderListRowVO> filtered = workspaceDashboardMapper.selectWorkspaceOrders(resolvedCampId, trimToNull(keyword)).stream()
                .filter(row -> matchesWorkspaceOrderType(row, orderType, today))
                .sorted(Comparator
                        .comparing(WorkspaceOrderListRowVO::getStartAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(WorkspaceOrderListRowVO::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(WorkspaceOrderListRowVO::getOrderId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        int fromIndex = Math.min((resolvedPageNum - 1) * resolvedPageSize, filtered.size());
        int toIndex = Math.min(fromIndex + resolvedPageSize, filtered.size());

        WorkspaceOrdersResponseVO response = new WorkspaceOrdersResponseVO();
        response.setTotal((long) filtered.size());
        response.setList(filtered.subList(fromIndex, toIndex).stream()
                .map(row -> toWorkspaceOrderItem(row, today))
                .toList());
        response.setPagination(toPagination(resolvedPageNum, resolvedPageSize, (long) filtered.size()));
        return response;
    }

    @Override
    public WorkspaceMemoPageResponseVO getMemoPage(
            Long campId,
            Long userId,
            Integer page,
            Integer pageNum,
            Integer current,
            Integer pageSize
    ) {
        resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = normalizePageNum(page, pageNum, current);
        int resolvedPageSize = normalizePageSize(pageSize);

        WorkspaceMemoPageResponseVO response = new WorkspaceMemoPageResponseVO();
        response.setTotal(0L);
        response.setList(List.<WorkspaceMemoItemVO>of());
        response.setPagination(toPagination(resolvedPageNum, resolvedPageSize, 0L));
        return response;
    }

    @Override
    public List<WorkspaceBacklogItemVO> getBacklogs(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<ChannelVO> channels = platformBootstrapMapper.selectChannelsByCampId(resolvedCampId);
        Map<Long, Boolean> channelAuthorization = new LinkedHashMap<>();
        for (ChannelVO channel : channels) {
            if (channel.getChannelId() == null) {
                continue;
            }
            channelAuthorization.merge(channel.getChannelId(), isAuthorized(channel.getStatus()), Boolean::logicalOr);
        }

        List<WorkspaceBacklogItemVO> items = new ArrayList<>();
        long pendingOpenChannels = channelAuthorization.values().stream().filter(Boolean.FALSE::equals).count();
        if (pendingOpenChannels > 0) {
            items.add(toBacklogItem(
                    "还有 " + pendingOpenChannels + " 个渠道待开通",
                    "开通后可同步更多流量入口并减少人工维护",
                    "立即开通"
            ));
        }

        int exceptionOrderCount = Optional.ofNullable(workspaceDashboardMapper.countExceptionOrders(resolvedCampId)).orElse(0);
        if (exceptionOrderCount > 0) {
            items.add(toBacklogItem(
                    "有 " + exceptionOrderCount + " 个异常订单待处理",
                    "请尽快核查退款中、已取消或已退款订单",
                    "查看订单"
            ));
        }
        return items;
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
            throw new BusinessException(40301, "no access to current camp workspace dashboard");
        }
        return requestedCampId;
    }

    private int resolveRoomCount(Long campId) {
        Integer activeRoomCount = workspaceDashboardMapper.countActiveRooms(campId);
        if (activeRoomCount != null && activeRoomCount > 0) {
            return activeRoomCount;
        }
        return Optional.ofNullable(workspaceDashboardMapper.sumRoomCategoryCount(campId)).orElse(0);
    }

    private int normalizePageNum(Integer page, Integer pageNum, Integer current) {
        Integer candidate = page != null ? page : (pageNum != null ? pageNum : current);
        return candidate == null || candidate < 1 ? DEFAULT_PAGE_NUM : candidate;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private LocalDate parseRequiredDate(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(40001, errorMessage);
        }
        return LocalDate.parse(value.trim().substring(0, 10));
    }

    private int count(List<WorkspaceDashboardRoomRowVO> rows, java.util.function.Predicate<WorkspaceDashboardRoomRowVO> predicate) {
        return Math.toIntExact(rows.stream().filter(predicate).count());
    }

    private boolean matchesWorkspaceOrderType(WorkspaceOrderListRowVO row, String orderType, LocalDate today) {
        String normalized = trimToNull(orderType);
        if (normalized == null) {
            return true;
        }
        return switch (normalized) {
            case "11" -> isBooked(row) && isSameDate(row.getStartAt(), today);
            case "12" -> isStaying(row, today);
            case "13" -> isCheckingOut(row, today);
            default -> false;
        };
    }

    private boolean isBooked(WorkspaceDashboardRoomRowVO row) {
        String status = trimToNull(row.getOrderStatus());
        return "booked".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status);
    }

    private boolean isCheckedIn(WorkspaceDashboardRoomRowVO row) {
        return "checked_in".equalsIgnoreCase(trimToNull(row.getOrderStatus()));
    }

    private boolean isBooked(WorkspaceOrderListRowVO row) {
        String status = trimToNull(row.getOrderStatus());
        return "booked".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status);
    }

    private boolean isCheckedIn(WorkspaceOrderListRowVO row) {
        return "checked_in".equalsIgnoreCase(trimToNull(row.getOrderStatus()));
    }

    private boolean isSameDate(LocalDateTime value, LocalDate targetDate) {
        return value != null && value.toLocalDate().isEqual(targetDate);
    }

    private boolean isStaying(WorkspaceOrderListRowVO row, LocalDate today) {
        if (!isCheckedIn(row) || row.getStartAt() == null || row.getEndAt() == null) {
            return false;
        }
        LocalDate startDate = row.getStartAt().toLocalDate();
        LocalDate endDate = row.getEndAt().toLocalDate();
        return !startDate.isAfter(today) && endDate.isAfter(today);
    }

    private boolean isCheckingOut(WorkspaceOrderListRowVO row, LocalDate today) {
        return isCheckedIn(row) && isSameDate(row.getEndAt(), today);
    }

    private boolean isOnSale(WorkspaceDashboardRoomRowVO row) {
        boolean idle = trimToNull(row.getOrderId()) == null;
        return idle && !isDirty(row.getCleanStatus()) && !isRepair(row.getLockStatus());
    }

    private boolean isDirty(String cleanStatus) {
        return "dirty".equalsIgnoreCase(trimToNull(cleanStatus));
    }

    private boolean isRepair(String lockStatus) {
        String normalized = trimToNull(lockStatus);
        if (normalized == null) {
            return false;
        }
        String lower = normalized.toLowerCase();
        return REPAIR_KEYWORDS.stream().anyMatch(lower::contains);
    }

    private boolean isAuthorized(String status) {
        return AUTHORIZED_STATUS.equalsIgnoreCase(trimToNull(status));
    }

    private Long sumBusinessIncomeCent(List<WorkspaceDashboardOrderRowVO> rows, LocalDate rangeStart, LocalDate rangeEnd) {
        long total = 0L;
        for (WorkspaceDashboardOrderRowVO row : rows) {
            OverlapAllocation allocation = allocate(row, rangeStart, rangeEnd);
            if (allocation == null) {
                continue;
            }
            total += allocation.businessIncomeCent();
        }
        return total;
    }

    private OverlapAllocation allocate(WorkspaceDashboardOrderRowVO row, LocalDate rangeStart, LocalDate rangeEnd) {
        if (row.getStartAt() == null || row.getEndAt() == null) {
            return null;
        }

        LocalDate orderStart = row.getStartAt().toLocalDate();
        LocalDate orderEndExclusive = row.getEndAt().toLocalDate();
        if (!orderEndExclusive.isAfter(orderStart)) {
            orderEndExclusive = orderStart.plusDays(resolvePositiveDayNum(row.getDayNum()));
        }

        LocalDate effectiveStart = max(orderStart, rangeStart);
        LocalDate effectiveEndExclusive = min(orderEndExclusive, rangeEnd.plusDays(1));
        if (!effectiveEndExclusive.isAfter(effectiveStart)) {
            return null;
        }

        int totalNights = Math.max(resolvePositiveDayNum(row.getDayNum()), (int) ChronoUnit.DAYS.between(orderStart, orderEndExclusive));
        BigDecimal grossIncome = toAmount(defaultLong(row.getTotalPriceCent()) + defaultLong(row.getCommissionPriceCent()));
        BigDecimal businessIncome = toAmount(defaultLong(row.getBusinessIncomeCent()));
        long grossIncomeCent = defaultLong(row.getTotalPriceCent()) + defaultLong(row.getCommissionPriceCent());
        long businessIncomeCent = defaultLong(row.getBusinessIncomeCent());

        BigDecimal allocatedGrossIncome = BigDecimal.ZERO;
        BigDecimal allocatedBusinessIncome = BigDecimal.ZERO;
        long allocatedGrossIncomeCent = 0L;
        long allocatedBusinessIncomeCent = 0L;
        int openRoomCount = 0;
        Map<LocalDate, DailyShare> dailyShares = new LinkedHashMap<>();

        LocalDate cursor = effectiveStart;
        while (cursor.isBefore(effectiveEndExclusive)) {
            BigDecimal ratio = BigDecimal.ONE.divide(BigDecimal.valueOf(totalNights), 10, RoundingMode.HALF_UP);
            BigDecimal grossShare = grossIncome.multiply(ratio);
            BigDecimal businessShare = businessIncome.multiply(ratio);
            long grossShareCent = Math.round((double) grossIncomeCent / totalNights);
            long businessShareCent = Math.round((double) businessIncomeCent / totalNights);

            allocatedGrossIncome = allocatedGrossIncome.add(grossShare);
            allocatedBusinessIncome = allocatedBusinessIncome.add(businessShare);
            allocatedGrossIncomeCent += grossShareCent;
            allocatedBusinessIncomeCent += businessShareCent;
            openRoomCount++;
            dailyShares.put(cursor, new DailyShare(grossShare, businessShare, 1));
            cursor = cursor.plusDays(1);
        }

        if (!dailyShares.isEmpty()) {
            LocalDate lastDate = null;
            for (LocalDate date : dailyShares.keySet()) {
                lastDate = date;
            }
            if (lastDate != null) {
                long grossDiffCent = grossIncomeCent * openRoomCount / totalNights - allocatedGrossIncomeCent;
                long businessDiffCent = businessIncomeCent * openRoomCount / totalNights - allocatedBusinessIncomeCent;
                if (grossDiffCent != 0L || businessDiffCent != 0L) {
                    DailyShare lastShare = dailyShares.get(lastDate);
                    dailyShares.put(lastDate, new DailyShare(
                            lastShare.grossIncome().add(toAmount(grossDiffCent)),
                            lastShare.businessIncome().add(toAmount(businessDiffCent)),
                            lastShare.openRoomCount()
                    ));
                    allocatedGrossIncome = allocatedGrossIncome.add(toAmount(grossDiffCent));
                    allocatedBusinessIncome = allocatedBusinessIncome.add(toAmount(businessDiffCent));
                    allocatedGrossIncomeCent += grossDiffCent;
                    allocatedBusinessIncomeCent += businessDiffCent;
                }
            }
        }

        return new OverlapAllocation(
                allocatedGrossIncome,
                allocatedBusinessIncome,
                allocatedGrossIncomeCent,
                allocatedBusinessIncomeCent,
                openRoomCount,
                dailyShares
        );
    }

    private int resolvePositiveDayNum(Integer dayNum) {
        return dayNum == null || dayNum < 1 ? 1 : dayNum;
    }

    private BigDecimal dividePercentage(BigDecimal numerator, int denominator) {
        if (denominator <= 0) {
            return zeroAmount();
        }
        return numerator.multiply(ONE_HUNDRED).divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal divideAmount(BigDecimal amount, int denominator) {
        if (denominator <= 0) {
            return zeroAmount();
        }
        return amount.divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroAmount() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal toAmount(long amountCent) {
        return BigDecimal.valueOf(amountCent, 2);
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private String resolveChannelName(WorkspaceDashboardOrderRowVO row) {
        return defaultString(row.getChannelName(), mapChannelName(row.getSourceType()));
    }

    private WorkspaceOrderItemVO toWorkspaceOrderItem(WorkspaceOrderListRowVO row, LocalDate today) {
        String statusName = resolveWorkspaceOrderStatusName(row, today);

        WorkspaceOrderItemVO item = new WorkspaceOrderItemVO();
        item.setChannelName(defaultString(row.getChannelName(), "未知渠道"));
        item.setGuestName(defaultString(row.getGuestName(), "-"));
        item.setGuestMobile(defaultString(row.getGuestMobile(), "-"));
        item.setRoomCategoryName(defaultString(row.getRoomCategoryName(), "-"));
        item.setRoomName(defaultString(row.getRoomName(), "-"));
        item.setStartTime(toEpochMillis(row.getStartAt()));
        item.setEndTime(toEpochMillis(row.getEndAt()));
        item.setDayNum(resolvePositiveDayNum(row.getDayNum()));
        item.setOrderDetailDisplayStateName(statusName);
        item.setStatusName(statusName);
        return item;
    }

    private String resolveWorkspaceOrderStatusName(WorkspaceOrderListRowVO row, LocalDate today) {
        if (isBooked(row) && isSameDate(row.getStartAt(), today)) {
            return "待入住";
        }
        if (isCheckingOut(row, today)) {
            return "待退房";
        }
        if (isCheckedIn(row)) {
            return "在住";
        }
        return defaultString(row.getOrderStatus(), "待确认");
    }

    private String mapChannelName(String sourceType) {
        String normalized = trimToNull(sourceType);
        if (normalized == null) {
            return "未知渠道";
        }
        return switch (normalized) {
            case "frontdesk" -> "自来客";
            case "phone" -> "电话订单";
            default -> normalized;
        };
    }

    private String defaultString(String value, String fallback) {
        return trimToNull(value) == null ? fallback : value.trim();
    }

    private WorkspacePaginationVO toPagination(int page, int pageSize, long total) {
        WorkspacePaginationVO pagination = new WorkspacePaginationVO();
        pagination.setPage(page);
        pagination.setPageSize(pageSize);
        pagination.setTotal(total);
        return pagination;
    }

    private WorkspaceBacklogItemVO toBacklogItem(String title, String subTitle, String button) {
        WorkspaceBacklogItemVO item = new WorkspaceBacklogItemVO();
        item.setContent("{\"title\":\"" + escapeJson(title) + "\",\"sub_title\":\"" + escapeJson(subTitle) + "\",\"button\":\"" + escapeJson(button) + "\"}");
        return item;
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
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

    private Long toEpochMillis(LocalDateTime value) {
        return value == null ? null : value.atZone(SHANGHAI_ZONE).toInstant().toEpochMilli();
    }

    private static final class DailyAccumulator {
        private final LocalDate date;
        private final int inventory;
        private BigDecimal businessIncome = BigDecimal.ZERO;
        private int openRoomCount = 0;

        private DailyAccumulator(LocalDate date, int activeRoomCount) {
            this.date = date;
            this.inventory = Math.max(activeRoomCount, 0);
        }
    }

    private record DailyShare(BigDecimal grossIncome, BigDecimal businessIncome, int openRoomCount) {
    }

    private record OverlapAllocation(
            BigDecimal grossIncome,
            BigDecimal businessIncome,
            long grossIncomeCent,
            long businessIncomeCent,
            int openRoomCount,
            Map<LocalDate, DailyShare> dailyShares
    ) {
    }
}
