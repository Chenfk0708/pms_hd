package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.CleanTaskMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.CleanTaskService;
import com.jeez.zp.platform.vo.CleanTaskDashboardResponseVO;
import com.jeez.zp.platform.vo.CleanTaskOptionVO;
import com.jeez.zp.platform.vo.CleanTaskPaginationVO;
import com.jeez.zp.platform.vo.CleanTaskQueryRowVO;
import com.jeez.zp.platform.vo.CleanTaskRecordVO;
import com.jeez.zp.platform.vo.CleanTaskSummaryVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CleanTaskServiceImpl implements CleanTaskService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String TYPE_ALL = "ALL";
    private static final String TYPE_CHECKOUT = "CHECKOUT";
    private static final String TYPE_STAY = "STAY";
    private static final String TYPE_PLAN = "PLAN";
    private static final String TYPE_TEMPORARY = "TEMPORARY";
    private static final String STATUS_ALL = "ALL";
    private static final String STATUS_PENDING_ASSIGN = "PENDING_ASSIGN";
    private static final String STATUS_PENDING_CLEAN = "PENDING_CLEAN";
    private static final String STATUS_CLEANING = "CLEANING";
    private static final String STATUS_DONE = "DONE";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final CleanTaskMapper cleanTaskMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public CleanTaskDashboardResponseVO getPage(
            Long campId,
            Long userId,
            Long poiId,
            String cleanTime,
            Long roomId,
            String cleanType,
            String cleanStatus,
            List<String> cleanerIds,
            Integer pageNum,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = normalizePageNum(pageNum);
        int resolvedPageSize = normalizePageSize(pageSize);
        LocalDate cleanDate = parseDate(cleanTime);

        List<CleanTaskRecordVO> filteredRecords = cleanTaskMapper.selectTaskRows(
                        resolvedCampId,
                        poiId,
                        roomId,
                        parseLongList(cleanerIds),
                        cleanDate
                ).stream()
                .map(this::toRecord)
                .filter(record -> matchesCleanType(record, cleanType))
                .filter(record -> matchesCleanStatus(record, cleanStatus))
                .sorted(Comparator
                        .comparing(CleanTaskRecordVO::getCleanDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(CleanTaskRecordVO::getDeadline, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(CleanTaskRecordVO::getTaskId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        PageSlice<CleanTaskRecordVO> pageSlice = pageSlice(filteredRecords, resolvedPageNum, resolvedPageSize);

        CleanTaskDashboardResponseVO response = new CleanTaskDashboardResponseVO();
        response.setStores(buildStores(resolvedCampId));
        response.setRooms(cleanTaskMapper.selectRooms(resolvedCampId));
        response.setCleanTypes(buildCleanTypes());
        response.setStatuses(buildStatuses());
        response.setCleaners(cleanTaskMapper.selectCleaners(resolvedCampId));
        response.setSummary(buildSummary(filteredRecords));
        response.setList(pageSlice.items());
        response.setPagination(toPagination(pageSlice.total(), resolvedPageNum, resolvedPageSize));
        return response;
    }

    private List<CleanTaskOptionVO> buildStores(Long campId) {
        List<CleanTaskOptionVO> stores = new ArrayList<>();
        stores.add(new CleanTaskOptionVO("ALL", "全部门店"));
        stores.addAll(cleanTaskMapper.selectStores(campId));
        return stores;
    }

    private List<CleanTaskOptionVO> buildCleanTypes() {
        return List.of(
                new CleanTaskOptionVO(TYPE_ALL, "全部类型"),
                new CleanTaskOptionVO(TYPE_CHECKOUT, "退房保洁"),
                new CleanTaskOptionVO(TYPE_STAY, "续住保洁"),
                new CleanTaskOptionVO(TYPE_PLAN, "计划保洁"),
                new CleanTaskOptionVO(TYPE_TEMPORARY, "临时保洁")
        );
    }

    private List<CleanTaskOptionVO> buildStatuses() {
        return List.of(
                new CleanTaskOptionVO(STATUS_ALL, "全部状态"),
                new CleanTaskOptionVO(STATUS_PENDING_ASSIGN, "待分配"),
                new CleanTaskOptionVO(STATUS_PENDING_CLEAN, "待保洁"),
                new CleanTaskOptionVO(STATUS_CLEANING, "保洁中"),
                new CleanTaskOptionVO(STATUS_DONE, "已完成"),
                new CleanTaskOptionVO(STATUS_CANCELLED, "已取消")
        );
    }

    private CleanTaskRecordVO toRecord(CleanTaskQueryRowVO row) {
        String cleanType = mapCleanType(row.getTaskType());
        String cleanStatus = mapCleanStatus(row.getTaskStatus(), row.getCleanerId());
        LocalDateTime deadlineAt = row.getDeadlineAt();

        CleanTaskRecordVO record = new CleanTaskRecordVO();
        record.setTaskId(row.getTaskId());
        record.setTaskNo("CT" + row.getTaskId());
        record.setRoomName(row.getRoomCategoryName() + " / " + row.getRoomName());
        record.setPoiName(row.getPoiName());
        record.setCleanType(cleanType);
        record.setCleanStatus(cleanStatus);
        record.setCleanerId(row.getCleanerId());
        record.setCleanerName(row.getCleanerName() == null || row.getCleanerName().isBlank() ? "待分配" : row.getCleanerName());
        record.setCleanDate(deadlineAt == null ? "" : deadlineAt.toLocalDate().format(DATE_FORMATTER));
        record.setPlanTime(deadlineAt == null ? "-" : deadlineAt.format(TIME_FORMATTER));
        record.setDeadline(deadlineAt == null ? "-" : deadlineAt.format(TIME_FORMATTER));
        record.setSourceOrderNo("-");
        record.setGuestName("-");
        record.setRemark(row.getRemark());
        record.setProgress(resolveProgress(cleanStatus));
        record.setPriority(resolvePriority(cleanType, cleanStatus));
        return record;
    }

    private boolean matchesCleanType(CleanTaskRecordVO record, String cleanType) {
        return cleanType == null || cleanType.isBlank() || TYPE_ALL.equals(cleanType) || cleanType.equals(record.getCleanType());
    }

    private boolean matchesCleanStatus(CleanTaskRecordVO record, String cleanStatus) {
        return cleanStatus == null || cleanStatus.isBlank() || STATUS_ALL.equals(cleanStatus) || cleanStatus.equals(record.getCleanStatus());
    }

    private String mapCleanType(String taskType) {
        if ("checkout_clean".equals(taskType)) {
            return TYPE_CHECKOUT;
        }
        if ("daily_clean".equals(taskType)) {
            return TYPE_STAY;
        }
        if ("night_clean".equals(taskType)) {
            return TYPE_TEMPORARY;
        }
        return TYPE_PLAN;
    }

    private String mapCleanStatus(String taskStatus, String cleanerId) {
        if ("done".equals(taskStatus)) {
            return STATUS_DONE;
        }
        if ("processing".equals(taskStatus)) {
            return STATUS_CLEANING;
        }
        if ("cancelled".equals(taskStatus)) {
            return STATUS_CANCELLED;
        }
        if (cleanerId == null || cleanerId.isBlank()) {
            return STATUS_PENDING_ASSIGN;
        }
        return STATUS_PENDING_CLEAN;
    }

    private int resolveProgress(String cleanStatus) {
        return switch (cleanStatus) {
            case STATUS_PENDING_ASSIGN -> 0;
            case STATUS_PENDING_CLEAN -> 20;
            case STATUS_CLEANING -> 65;
            case STATUS_DONE -> 100;
            default -> 0;
        };
    }

    private String resolvePriority(String cleanType, String cleanStatus) {
        if (TYPE_CHECKOUT.equals(cleanType) && !STATUS_DONE.equals(cleanStatus)) {
            return "urgent";
        }
        return "normal";
    }

    private CleanTaskSummaryVO buildSummary(List<CleanTaskRecordVO> records) {
        CleanTaskSummaryVO summary = new CleanTaskSummaryVO();
        summary.setTotal(records.size());
        summary.setPendingAssign((int) records.stream().filter(record -> STATUS_PENDING_ASSIGN.equals(record.getCleanStatus())).count());
        summary.setPendingClean((int) records.stream().filter(record -> STATUS_PENDING_CLEAN.equals(record.getCleanStatus())).count());
        summary.setCleaning((int) records.stream().filter(record -> STATUS_CLEANING.equals(record.getCleanStatus())).count());
        summary.setDone((int) records.stream().filter(record -> STATUS_DONE.equals(record.getCleanStatus())).count());
        summary.setOverdue((int) records.stream().filter(record -> "urgent".equals(record.getPriority()) && !STATUS_DONE.equals(record.getCleanStatus())).count());
        return summary;
    }

    private CleanTaskPaginationVO toPagination(long total, int pageNum, int pageSize) {
        CleanTaskPaginationVO pagination = new CleanTaskPaginationVO();
        pagination.setPage(pageNum);
        pagination.setPageSize(pageSize);
        pagination.setTotal(total);
        return pagination;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private LocalDate parseDate(String cleanTime) {
        if (cleanTime == null || cleanTime.isBlank()) {
            return null;
        }
        return LocalDate.parse(cleanTime, DATE_FORMATTER);
    }

    private List<Long> parseLongList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<Long> parsed = values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(Long::valueOf)
                .distinct()
                .toList();
        return parsed.isEmpty() ? null : parsed;
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
            throw new BusinessException(40301, "无权访问当前门店保洁任务");
        }
        return requestedCampId;
    }

    private <T> PageSlice<T> pageSlice(List<T> items, int pageNum, int pageSize) {
        long total = items.size();
        int fromIndex = Math.min((pageNum - 1) * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return new PageSlice<>(total, items.subList(fromIndex, toIndex));
    }

    private record PageSlice<T>(long total, List<T> items) {
    }
}
