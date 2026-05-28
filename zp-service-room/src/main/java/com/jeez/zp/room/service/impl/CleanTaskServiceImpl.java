package com.jeez.zp.room.service.impl;

import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.CleanTaskMapper;
import com.jeez.zp.room.mapper.UserCampMapper;
import com.jeez.zp.room.service.CleanTaskService;
import com.jeez.zp.room.vo.CleanTaskDashboardResponseVO;
import com.jeez.zp.room.vo.CleanTaskOptionVO;
import com.jeez.zp.room.vo.CleanTaskPaginationVO;
import com.jeez.zp.room.vo.CleanTaskQueryRowVO;
import com.jeez.zp.room.vo.CleanTaskRecordVO;
import com.jeez.zp.room.vo.CleanTaskSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final CleanTaskMapper cleanTaskMapper;
    private final UserCampMapper userCampMapper;

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
        stores.add(new CleanTaskOptionVO("ALL", "\u5168\u90E8\u95E8\u5E97"));
        stores.addAll(cleanTaskMapper.selectStores(campId));
        return stores;
    }

    private List<CleanTaskOptionVO> buildCleanTypes() {
        return List.of(
                new CleanTaskOptionVO(TYPE_ALL, "\u5168\u90E8\u7C7B\u578B"),
                new CleanTaskOptionVO(TYPE_CHECKOUT, "\u9000\u623F\u4FDD\u6D01"),
                new CleanTaskOptionVO(TYPE_STAY, "\u7EED\u4F4F\u4FDD\u6D01"),
                new CleanTaskOptionVO(TYPE_PLAN, "\u8BA1\u5212\u4FDD\u6D01"),
                new CleanTaskOptionVO(TYPE_TEMPORARY, "\u4E34\u65F6\u4FDD\u6D01")
        );
    }

    private List<CleanTaskOptionVO> buildStatuses() {
        return List.of(
                new CleanTaskOptionVO(STATUS_ALL, "\u5168\u90E8\u72B6\u6001"),
                new CleanTaskOptionVO(STATUS_PENDING_ASSIGN, "\u5F85\u5206\u914D"),
                new CleanTaskOptionVO(STATUS_PENDING_CLEAN, "\u5F85\u4FDD\u6D01"),
                new CleanTaskOptionVO(STATUS_CLEANING, "\u4FDD\u6D01\u4E2D"),
                new CleanTaskOptionVO(STATUS_DONE, "\u5DF2\u5B8C\u6210"),
                new CleanTaskOptionVO(STATUS_CANCELLED, "\u5DF2\u53D6\u6D88")
        );
    }

    private CleanTaskRecordVO toRecord(CleanTaskQueryRowVO row) {
        String cleanType = mapCleanType(row.getTaskType());
        String cleanStatus = mapCleanStatus(row.getTaskStatus(), row.getCleanerId());

        CleanTaskRecordVO record = new CleanTaskRecordVO();
        record.setTaskId(row.getTaskId());
        record.setTaskNo("CT" + row.getTaskId());
        record.setRoomName(row.getRoomCategoryName() + " / " + row.getRoomName());
        record.setPoiName(row.getPoiName());
        record.setCleanType(cleanType);
        record.setCleanStatus(cleanStatus);
        record.setCleanerId(row.getCleanerId());
        record.setCleanerName(row.getCleanerName() == null || row.getCleanerName().isBlank() ? "\u5F85\u5206\u914D" : row.getCleanerName());
        record.setCleanDate(row.getDeadlineAt() == null ? "" : row.getDeadlineAt().toLocalDate().format(DATE_FORMATTER));
        record.setPlanTime(row.getDeadlineAt() == null ? "-" : row.getDeadlineAt().format(TIME_FORMATTER));
        record.setDeadline(row.getDeadlineAt() == null ? "-" : row.getDeadlineAt().format(TIME_FORMATTER));
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
        Long currentCampId = userCampMapper.selectCurrentCampId(userId);
        if (currentCampId == null) {
            throw new BusinessException(40401, "\u672A\u627E\u5230\u5F53\u524D\u7528\u6237\u95E8\u5E97");
        }
        if (requestedCampId == null) {
            return currentCampId;
        }
        if (!requestedCampId.equals(currentCampId)) {
            throw new BusinessException(40301, "\u65E0\u6743\u8BBF\u95EE\u5F53\u524D\u95E8\u5E97\u4FDD\u6D01\u4EFB\u52A1");
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
