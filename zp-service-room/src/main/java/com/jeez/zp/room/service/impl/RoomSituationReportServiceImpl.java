package com.jeez.zp.room.service.impl;

import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.RoomSituationReportMapper;
import com.jeez.zp.room.mapper.UserCampMapper;
import com.jeez.zp.room.service.RoomSituationReportService;
import com.jeez.zp.room.vo.DailyRoomStatusRowVO;
import com.jeez.zp.room.vo.ForwardRoomStatusDayVO;
import com.jeez.zp.room.vo.ForwardRoomStatusRowVO;
import com.jeez.zp.room.vo.RoomSituationInventoryRowVO;
import com.jeez.zp.room.vo.RoomSituationOrderRowVO;
import com.jeez.zp.room.vo.RoomSituationPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoomSituationReportServiceImpl implements RoomSituationReportService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final List<String> RETAIN_KEYWORDS = List.of("retain", "reserved", "reserve", "保留");
    private static final List<String> REPAIR_KEYWORDS = List.of("repair", "maintain", "offline", "disable", "维修", "停用", "故障");
    private static final List<String> LINKED_KEYWORDS = List.of("linked", "mainvice", "main_vice", "联动");

    private final RoomSituationReportMapper roomSituationReportMapper;
    private final UserCampMapper userCampMapper;

    @Override
    public RoomSituationPageResponseVO getDailyRoomStatus(
            Long campId,
            Long userId,
            String date,
            List<Long> poiIds,
            Integer pageNum,
            Integer current,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        LocalDate targetDate = parseRequiredDate(date, "date");
        LocalDateTime dayStart = targetDate.atStartOfDay();
        LocalDateTime nextDayStart = targetDate.plusDays(1).atStartOfDay();
        List<Long> normalizedPoiIds = normalizeIds(poiIds);

        List<RoomSituationInventoryRowVO> inventoryRows = roomSituationReportMapper.selectInventoryRows(resolvedCampId, normalizedPoiIds);
        List<RoomSituationOrderRowVO> orderRows = roomSituationReportMapper.selectOrderRows(
                resolvedCampId,
                dayStart,
                nextDayStart,
                normalizedPoiIds
        );

        Map<String, RoomSituationOrderRowVO> dailyOrderByRoomId = buildTopOrderByRoom(orderRows);
        List<DailyRoomStatusRowVO> rows = buildDailyRows(inventoryRows, dailyOrderByRoomId, targetDate);
        PageSlice<DailyRoomStatusRowVO> pageSlice = slice(rows, pageNum, current, pageSize);
        return toResponse(pageSlice, normalizePageNum(pageNum, current), normalizePageSize(pageSize));
    }

    @Override
    public RoomSituationPageResponseVO getForwardRoomStatus(
            Long campId,
            Long userId,
            String startDate,
            String endDate,
            List<Long> poiIds,
            Integer pageNum,
            Integer current,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        LocalDate rangeStart = parseRequiredDate(startDate, "startDate");
        LocalDate parsedEnd = parseRequiredDate(endDate, "endDate");
        LocalDate rangeEndExclusive = parsedEnd.isAfter(rangeStart) ? parsedEnd : rangeStart.plusDays(1);
        List<Long> normalizedPoiIds = normalizeIds(poiIds);

        List<RoomSituationInventoryRowVO> inventoryRows = roomSituationReportMapper.selectInventoryRows(resolvedCampId, normalizedPoiIds);
        List<RoomSituationOrderRowVO> orderRows = roomSituationReportMapper.selectOrderRows(
                resolvedCampId,
                rangeStart.atStartOfDay(),
                rangeEndExclusive.atStartOfDay(),
                normalizedPoiIds
        );

        Map<String, List<RoomSituationOrderRowVO>> orderRowsByRoomId = orderRows.stream()
                .collect(LinkedHashMap::new, (map, row) -> map.computeIfAbsent(row.getRoomId(), ignored -> new ArrayList<>()).add(row), Map::putAll);
        List<ForwardRoomStatusRowVO> rows = buildForwardRows(inventoryRows, orderRowsByRoomId, rangeStart, rangeEndExclusive);
        PageSlice<ForwardRoomStatusRowVO> pageSlice = slice(rows, pageNum, current, pageSize);
        return toResponse(pageSlice, normalizePageNum(pageNum, current), normalizePageSize(pageSize));
    }

    private List<DailyRoomStatusRowVO> buildDailyRows(
            List<RoomSituationInventoryRowVO> inventoryRows,
            Map<String, RoomSituationOrderRowVO> dailyOrderByRoomId,
            LocalDate targetDate
    ) {
        List<DailyRoomStatusRowVO> rows = new ArrayList<>();
        for (CategoryBucket bucket : groupByCategory(inventoryRows).values()) {
            DailyRoomStatusRowVO row = new DailyRoomStatusRowVO();
            row.setRoomCategoryId(bucket.roomCategoryId());
            row.setRoomCategoryName(bucket.roomCategoryName());
            row.setAvailabilityCount(bucket.rooms().size());

            int occupiedCount = 0;
            int availableCount = 0;
            int retainCount = 0;
            int repairCount = 0;
            int linkedCount = 0;
            int cleanCount = 0;
            int dirtyCount = 0;
            int preComeCount = 0;
            int liveCount = 0;
            int preLeaveCount = 0;

            for (RoomSituationInventoryRowVO room : bucket.rooms()) {
                BusyType busyType = resolveBusyType(room.getLockStatus());
                RoomSituationOrderRowVO order = dailyOrderByRoomId.get(room.getRoomId());
                boolean occupied = order != null;

                if (occupied) {
                    occupiedCount++;
                    if (isPreCome(order, targetDate)) {
                        preComeCount++;
                    }
                    if (isLive(order, targetDate)) {
                        liveCount++;
                    }
                    if (isPreLeave(order, targetDate)) {
                        preLeaveCount++;
                    }
                } else if (busyType == BusyType.NONE) {
                    availableCount++;
                }

                if (busyType == BusyType.RETAIN) {
                    retainCount++;
                } else if (busyType == BusyType.REPAIR) {
                    repairCount++;
                } else if (busyType == BusyType.LINKED) {
                    linkedCount++;
                }

                if (isDirty(room.getCleanStatus())) {
                    dirtyCount++;
                } else {
                    cleanCount++;
                }
            }

            row.setOpenRoomCount(occupiedCount);
            row.setRoomSaleCount(availableCount);
            row.setCloseRoomCount(bucket.rooms().size() - availableCount);
            row.setUserBusyRetainNum(retainCount);
            row.setUserBusyRepairNum(repairCount);
            row.setMainViceRelNum(linkedCount);
            row.setUserBusyNum(retainCount + repairCount + linkedCount);
            row.setTotalVacantRoomCount(bucket.rooms().size() - row.getUserBusyNum());
            row.setPreComeNum(preComeCount);
            row.setLiveNum(liveCount);
            row.setPreLeaveNum(preLeaveCount);
            row.setCleanNum(cleanCount);
            row.setDirtyNum(dirtyCount);
            rows.add(row);
        }
        return rows;
    }

    private List<ForwardRoomStatusRowVO> buildForwardRows(
            List<RoomSituationInventoryRowVO> inventoryRows,
            Map<String, List<RoomSituationOrderRowVO>> orderRowsByRoomId,
            LocalDate rangeStart,
            LocalDate rangeEndExclusive
    ) {
        List<ForwardRoomStatusRowVO> rows = new ArrayList<>();
        for (CategoryBucket bucket : groupByCategory(inventoryRows).values()) {
            ForwardRoomStatusRowVO row = new ForwardRoomStatusRowVO();
            row.setRoomCategoryId(bucket.roomCategoryId());
            row.setRoomCategoryName(bucket.roomCategoryName());
            row.setAvailabilityCount(bucket.rooms().size());

            List<ForwardRoomStatusDayVO> dayRows = new ArrayList<>();
            LocalDate cursor = rangeStart;
            while (cursor.isBefore(rangeEndExclusive)) {
                LocalDateTime dayStart = cursor.atStartOfDay();
                LocalDateTime nextDayStart = cursor.plusDays(1).atStartOfDay();
                int occupiedCount = 0;
                int availableCount = 0;

                for (RoomSituationInventoryRowVO room : bucket.rooms()) {
                    boolean occupied = orderRowsByRoomId.getOrDefault(room.getRoomId(), List.of()).stream()
                            .anyMatch(order -> overlaps(order, dayStart, nextDayStart));
                    if (occupied) {
                        occupiedCount++;
                    } else if (resolveBusyType(room.getLockStatus()) == BusyType.NONE) {
                        availableCount++;
                    }
                }

                ForwardRoomStatusDayVO dayRow = new ForwardRoomStatusDayVO();
                dayRow.setRoomSaleCount(availableCount);
                dayRow.setOccupationCount(occupiedCount);
                dayRows.add(dayRow);
                cursor = cursor.plusDays(1);
            }

            row.setForwardRoomStatusList(dayRows);
            rows.add(row);
        }
        return rows;
    }

    private Map<String, CategoryBucket> groupByCategory(List<RoomSituationInventoryRowVO> inventoryRows) {
        Map<String, CategoryBucket> buckets = new LinkedHashMap<>();
        for (RoomSituationInventoryRowVO row : inventoryRows) {
            CategoryBucket bucket = buckets.computeIfAbsent(
                    row.getRoomCategoryId(),
                    ignored -> new CategoryBucket(
                            row.getRoomCategoryId(),
                            row.getRoomCategoryName(),
                            row.getRoomCategorySeq(),
                            new ArrayList<>()
                    )
            );
            bucket.rooms().add(row);
        }
        return buckets;
    }

    private Map<String, RoomSituationOrderRowVO> buildTopOrderByRoom(List<RoomSituationOrderRowVO> orderRows) {
        Map<String, RoomSituationOrderRowVO> ordersByRoomId = new LinkedHashMap<>();
        orderRows.stream()
                .sorted(Comparator
                        .comparingInt((RoomSituationOrderRowVO row) -> resolveStatusPriority(row.getOrderStatus()))
                        .thenComparing(RoomSituationOrderRowVO::getStartAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(RoomSituationOrderRowVO::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(RoomSituationOrderRowVO::getOrderId, Comparator.nullsLast(Comparator.naturalOrder())))
                .forEach(row -> ordersByRoomId.putIfAbsent(row.getRoomId(), row));
        return ordersByRoomId;
    }

    private int resolveStatusPriority(String orderStatus) {
        String normalizedStatus = trimToNull(orderStatus);
        if ("checked_in".equalsIgnoreCase(normalizedStatus)) {
            return 1;
        }
        if ("booked".equalsIgnoreCase(normalizedStatus)) {
            return 2;
        }
        if ("pending".equalsIgnoreCase(normalizedStatus)) {
            return 3;
        }
        if ("refunding".equalsIgnoreCase(normalizedStatus)) {
            return 4;
        }
        return 9;
    }

    private boolean isPreCome(RoomSituationOrderRowVO row, LocalDate targetDate) {
        String status = trimToNull(row.getOrderStatus());
        return ("booked".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status))
                && isSameDate(row.getStartAt(), targetDate);
    }

    private boolean isLive(RoomSituationOrderRowVO row, LocalDate targetDate) {
        return "checked_in".equalsIgnoreCase(trimToNull(row.getOrderStatus()))
                && overlaps(row, targetDate.atStartOfDay(), targetDate.plusDays(1).atStartOfDay());
    }

    private boolean isPreLeave(RoomSituationOrderRowVO row, LocalDate targetDate) {
        return "checked_in".equalsIgnoreCase(trimToNull(row.getOrderStatus()))
                && isSameDate(row.getEndAt(), targetDate);
    }

    private boolean overlaps(RoomSituationOrderRowVO row, LocalDateTime rangeStart, LocalDateTime rangeEndExclusive) {
        return row.getStartAt() != null
                && row.getEndAt() != null
                && row.getStartAt().isBefore(rangeEndExclusive)
                && row.getEndAt().isAfter(rangeStart);
    }

    private boolean isSameDate(LocalDateTime dateTime, LocalDate targetDate) {
        return dateTime != null && dateTime.toLocalDate().isEqual(targetDate);
    }

    private boolean isDirty(String cleanStatus) {
        return "dirty".equalsIgnoreCase(trimToNull(cleanStatus));
    }

    private BusyType resolveBusyType(String lockStatus) {
        String normalized = trimToNull(lockStatus);
        if (normalized == null) {
            return BusyType.NONE;
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (matchesAny(lower, LINKED_KEYWORDS)) {
            return BusyType.LINKED;
        }
        if (matchesAny(lower, RETAIN_KEYWORDS)) {
            return BusyType.RETAIN;
        }
        if (matchesAny(lower, REPAIR_KEYWORDS)) {
            return BusyType.REPAIR;
        }
        return BusyType.NONE;
    }

    private boolean matchesAny(String value, List<String> keywords) {
        return keywords.stream().anyMatch(value::contains);
    }

    private RoomSituationPageResponseVO toResponse(PageSlice<?> pageSlice, int pageNum, int pageSize) {
        RoomSituationPageResponseVO response = new RoomSituationPageResponseVO();
        response.setTotal(pageSlice.total());
        response.setPageNum(pageNum);
        response.setCurrent(pageNum);
        response.setPageSize(pageSize);
        response.setSize(pageSize);
        response.setList(pageSlice.items());
        return response;
    }

    private <T> PageSlice<T> slice(List<T> rows, Integer pageNum, Integer current, Integer pageSize) {
        int resolvedPageNum = normalizePageNum(pageNum, current);
        int resolvedPageSize = normalizePageSize(pageSize);
        int fromIndex = Math.min((resolvedPageNum - 1) * resolvedPageSize, rows.size());
        int toIndex = Math.min(fromIndex + resolvedPageSize, rows.size());
        return new PageSlice<>((long) rows.size(), rows.subList(fromIndex, toIndex));
    }

    private int normalizePageNum(Integer pageNum, Integer current) {
        Integer candidate = pageNum != null ? pageNum : current;
        return candidate == null || candidate < 1 ? DEFAULT_PAGE_NUM : candidate;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream().filter(id -> id != null && id > 0).distinct().toList();
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

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        Long currentCampId = userCampMapper.selectCurrentCampId(userId);
        if (currentCampId == null) {
            throw new BusinessException(40401, "未找到当前用户门店");
        }
        if (requestedCampId == null) {
            return currentCampId;
        }
        if (!requestedCampId.equals(currentCampId)) {
            throw new BusinessException(40301, "无权访问当前门店房情报表");
        }
        return requestedCampId;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private enum BusyType {
        NONE,
        RETAIN,
        REPAIR,
        LINKED
    }

    private record CategoryBucket(
            String roomCategoryId,
            String roomCategoryName,
            Integer roomCategorySeq,
            List<RoomSituationInventoryRowVO> rooms
    ) {
    }

    private record PageSlice<T>(long total, List<T> items) {
    }
}
