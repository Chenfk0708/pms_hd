package com.jeez.zp.room.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.jeez.zp.room.dto.request.CleanTaskCreateRequest;
import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.CleanTaskMapper;
import com.jeez.zp.room.mapper.UserCampMapper;
import com.jeez.zp.room.service.CleanTaskService;
import com.jeez.zp.room.vo.CleanTaskActionResponseVO;
import com.jeez.zp.room.vo.CleanTaskDashboardResponseVO;
import com.jeez.zp.room.vo.CleanTaskExportResponseVO;
import com.jeez.zp.room.vo.CleanTaskOptionVO;
import com.jeez.zp.room.vo.CleanTaskPaginationVO;
import com.jeez.zp.room.vo.CleanTaskQueryRowVO;
import com.jeez.zp.room.vo.CleanTaskRecordVO;
import com.jeez.zp.room.vo.CleanTaskRoomRowVO;
import com.jeez.zp.room.vo.CleanTaskStatisticsResponseVO;
import com.jeez.zp.room.vo.CleanTaskStatisticsRowVO;
import com.jeez.zp.room.vo.CleanTaskSummaryVO;
import com.jeez.zp.room.vo.CleanStatisticsDashboardResponseVO;
import com.jeez.zp.room.vo.CleanStatisticsDetailRowVO;
import com.jeez.zp.room.vo.CleanStatisticsExportResponseVO;
import com.jeez.zp.room.vo.CleanStatisticsMetricVO;
import com.jeez.zp.room.vo.CleanStatisticsPayloadVO;
import com.jeez.zp.room.vo.CleanStatisticsTodoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final long FEE_TYPE_ONE = 3600L;
    private static final long FEE_TYPE_TWO = 6100L;
    private static final long FEE_TYPE_THREE = 6600L;
    private static final long FEE_TYPE_FOUR = 7600L;

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

    @Override
    @Transactional
    public CleanTaskActionResponseVO create(CleanTaskCreateRequest request, Long userId) {
        if (request == null) {
            throw new BusinessException(40001, "request is required");
        }
        Long resolvedCampId = resolveAccessibleCampId(parseLong(request.getCampId()), userId);
        Long roomId = parseRequiredLong(request.getRoomId(), "roomId");
        Long poiId = parseLong(request.getPoiId());
        CleanTaskRoomRowVO room = cleanTaskMapper.selectRoomForTask(resolvedCampId, poiId, roomId);
        if (room == null) {
            throw new BusinessException(40401, "\u623F\u95F4\u4E0D\u5B58\u5728\u6216\u4E0D\u53EF\u7528");
        }

        String taskStatus = mapTaskStatus(request.getCleanStatus());
        Long cleanStaffId = resolveCleanStaffId(resolvedCampId, request.getCleanerId(), request.getCleanStatus());
        Long cleanTaskId = IdWorker.getId();
        cleanTaskMapper.insertCleanTask(
                cleanTaskId,
                resolvedCampId,
                room.getPoiId(),
                room.getRoomId(),
                room.getRoomCategoryId(),
                cleanStaffId,
                mapTaskType(request.getCleanType()),
                taskStatus,
                parseDeadlineAt(request.getDeadlineAt(), request.getCleanTime(), request.getDeadline()),
                trimToNull(request.getRemark())
        );

        CleanTaskActionResponseVO response = new CleanTaskActionResponseVO();
        response.setTaskId(String.valueOf(cleanTaskId));
        response.setTaskNo("CT" + cleanTaskId);
        response.setCleanStatus(mapCleanStatus(taskStatus, cleanStaffId == null ? null : String.valueOf(cleanStaffId)));
        response.setMessage("\u4FDD\u6D01\u4EFB\u52A1\u521B\u5EFA\u6210\u529F");
        return response;
    }

    @Override
    @Transactional
    public CleanTaskActionResponseVO notify(Long campId, Long userId, List<String> taskIds) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<Long> parsedTaskIds = requireLongList(taskIds, "taskIds");
        List<String> existingTaskIds = cleanTaskMapper.selectTaskIds(resolvedCampId, parsedTaskIds);
        if (existingTaskIds.size() != parsedTaskIds.size()) {
            throw new BusinessException(40401, "\u4FDD\u6D01\u4EFB\u52A1\u4E0D\u5B58\u5728");
        }
        int notifiedCount = cleanTaskMapper.incrementNotifyCount(resolvedCampId, parsedTaskIds);
        if (notifiedCount != parsedTaskIds.size()) {
            throw new BusinessException(40001, "\u5DF2\u5B8C\u6210\u6216\u5DF2\u53D6\u6D88\u7684\u4FDD\u6D01\u4EFB\u52A1\u4E0D\u80FD\u901A\u77E5");
        }

        CleanTaskActionResponseVO response = new CleanTaskActionResponseVO();
        response.setNotifiedCount(notifiedCount);
        response.setTaskIds(existingTaskIds);
        response.setMessage("\u4FDD\u6D01\u4EFB\u52A1\u901A\u77E5\u6210\u529F");
        return response;
    }

    @Override
    public CleanTaskExportResponseVO export(
            Long campId,
            Long userId,
            Long poiId,
            String cleanTime,
            Long roomId,
            String cleanType,
            String cleanStatus,
            List<String> cleanerIds
    ) {
        CleanTaskDashboardResponseVO dashboard = getPage(
                campId,
                userId,
                poiId,
                cleanTime,
                roomId,
                cleanType,
                cleanStatus,
                cleanerIds,
                DEFAULT_PAGE_NUM,
                Integer.MAX_VALUE
        );
        String exportedDate = parseDate(cleanTime) == null ? LocalDate.now().format(DATE_FORMATTER) : parseDate(cleanTime).format(DATE_FORMATTER);

        CleanTaskExportResponseVO response = new CleanTaskExportResponseVO();
        response.setFileName("clean_tasks_" + exportedDate + ".csv");
        response.setContentType("text/csv");
        response.setTotal(dashboard.getList().size());
        response.setRows(dashboard.getList());
        return response;
    }

    @Override
    public CleanTaskStatisticsResponseVO getStatistics(
            Long campId,
            Long userId,
            Long poiId,
            List<String> roomIds,
            List<String> cleanerIds,
            Long cleanStartTime,
            Long cleanEndTime,
            Integer pageNum,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId, "\u65E0\u6743\u8BBF\u95EE\u5F53\u524D\u95E8\u5E97\u4FDD\u6D01\u7EDF\u8BA1");
        int resolvedPageNum = normalizePageNum(pageNum);
        int resolvedPageSize = normalizePageSize(pageSize);

        List<CleanTaskQueryRowVO> rows = cleanTaskMapper.selectTaskRowsByRange(
                resolvedCampId,
                poiId,
                parseLongList(roomIds),
                parseLongList(cleanerIds),
                toLocalDateTime(cleanStartTime),
                toLocalDateTime(cleanEndTime)
        );

        List<CleanTaskStatisticsRowVO> aggregatedRows = aggregateStatisticsRows(rows);
        PageSlice<CleanTaskStatisticsRowVO> pageSlice = pageSlice(aggregatedRows, resolvedPageNum, resolvedPageSize);

        CleanTaskStatisticsResponseVO response = new CleanTaskStatisticsResponseVO();
        response.setTotal(pageSlice.total());
        response.setPageNum(resolvedPageNum);
        response.setCurrent(resolvedPageNum);
        response.setSize(resolvedPageSize);
        response.setList(pageSlice.items());
        return response;
    }

    @Override
    public CleanStatisticsDashboardResponseVO getStatisticsDashboard(
            Long campId,
            Long userId,
            Long poiId,
            List<String> roomIds,
            List<String> cleanerIds,
            Long cleanStartTime,
            Long cleanEndTime,
            Integer pageNum,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId, "\u65E0\u6743\u8BBF\u95EE\u5F53\u524D\u95E8\u5E97\u4FDD\u6D01\u7EDF\u8BA1");
        int resolvedPageNum = normalizePageNum(pageNum);
        int resolvedPageSize = normalizePageSize(pageSize);

        List<CleanTaskQueryRowVO> rows = selectStatisticsRows(
                resolvedCampId,
                poiId,
                roomIds,
                cleanerIds,
                cleanStartTime,
                cleanEndTime
        );
        List<CleanTaskStatisticsRowVO> aggregatedRows = aggregateStatisticsRows(rows);
        PageSlice<CleanTaskStatisticsRowVO> pageSlice = pageSlice(aggregatedRows, resolvedPageNum, resolvedPageSize);

        CleanStatisticsPayloadVO statistics = new CleanStatisticsPayloadVO();
        statistics.setList(pageSlice.items());
        statistics.setDetailList(toStatisticsDetailRows(rows));
        statistics.setMetrics(buildStatisticsMetrics(rows));
        statistics.setTodos(buildStatisticsTodos(rows));
        statistics.setPagination(toPagination(pageSlice.total(), resolvedPageNum, resolvedPageSize));

        CleanStatisticsDashboardResponseVO response = new CleanStatisticsDashboardResponseVO();
        response.setStatistics(statistics);
        response.setStores(buildStores(resolvedCampId));
        response.setRooms(cleanTaskMapper.selectRooms(resolvedCampId));
        response.setCleaners(cleanTaskMapper.selectCleaners(resolvedCampId));
        return response;
    }

    @Override
    public CleanStatisticsExportResponseVO exportStatistics(
            Long campId,
            Long userId,
            Long poiId,
            List<String> roomIds,
            List<String> cleanerIds,
            Long cleanStartTime,
            Long cleanEndTime
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId, "\u65E0\u6743\u8BBF\u95EE\u5F53\u524D\u95E8\u5E97\u4FDD\u6D01\u7EDF\u8BA1");
        List<CleanTaskQueryRowVO> rows = selectStatisticsRows(
                resolvedCampId,
                poiId,
                roomIds,
                cleanerIds,
                cleanStartTime,
                cleanEndTime
        );
        List<CleanStatisticsDetailRowVO> detailRows = toStatisticsDetailRows(rows);
        String exportedDate = resolveExportDate(cleanStartTime);

        CleanStatisticsExportResponseVO response = new CleanStatisticsExportResponseVO();
        response.setTaskId("CLEAN-STAT-EXPORT-" + exportedDate.replace("-", ""));
        response.setFileName("clean_statistics_" + exportedDate.replace("-", "") + ".csv");
        response.setContentType("text/csv");
        response.setDownloadUrl(buildStatisticsDownloadUrl(
                resolvedCampId,
                poiId,
                roomIds,
                cleanerIds,
                cleanStartTime,
                cleanEndTime
        ));
        response.setTotal(detailRows.size());
        response.setRows(detailRows);
        return response;
    }

    private String buildStatisticsDownloadUrl(
            Long campId,
            Long poiId,
            List<String> roomIds,
            List<String> cleanerIds,
            Long cleanStartTime,
            Long cleanEndTime
    ) {
        StringBuilder url = new StringBuilder("/api/clean/statistics/export/download")
                .append("?campId=").append(campId);
        appendQueryParam(url, "storeId", poiId);
        appendQueryParams(url, "roomIds", roomIds);
        appendQueryParams(url, "cleanerIds", cleanerIds);
        appendQueryParam(url, "cleanStartTime", cleanStartTime);
        appendQueryParam(url, "cleanEndTime", cleanEndTime);
        return url.toString();
    }

    private void appendQueryParam(StringBuilder url, String name, Long value) {
        if (value != null) {
            url.append("&").append(name).append("=").append(value);
        }
    }

    private void appendQueryParams(StringBuilder url, String name, List<String> values) {
        if (values == null) {
            return;
        }
        values.stream()
                .filter(value -> value != null && !value.isBlank())
                .forEach(value -> url.append("&").append(name).append("=").append(value));
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

    private String mapTaskType(String cleanType) {
        if (TYPE_CHECKOUT.equals(cleanType)) {
            return "checkout_clean";
        }
        if (TYPE_STAY.equals(cleanType)) {
            return "daily_clean";
        }
        if (TYPE_TEMPORARY.equals(cleanType)) {
            return "night_clean";
        }
        return "plan_clean";
    }

    private String mapTaskStatus(String cleanStatus) {
        if (STATUS_DONE.equals(cleanStatus)) {
            return "done";
        }
        if (STATUS_CLEANING.equals(cleanStatus)) {
            return "processing";
        }
        if (STATUS_CANCELLED.equals(cleanStatus)) {
            return "cancelled";
        }
        return "pending";
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

    private LocalDateTime parseDeadlineAt(String deadlineAt, String cleanTime, String deadline) {
        if (deadlineAt != null && !deadlineAt.isBlank()) {
            String normalized = deadlineAt.trim().replace('T', ' ');
            if (normalized.length() == 16) {
                normalized = normalized + ":00";
            }
            return LocalDateTime.parse(normalized, DATE_TIME_FORMATTER);
        }
        LocalDate date = parseDate(cleanTime);
        if (date == null) {
            throw new BusinessException(40001, "deadlineAt is required");
        }
        LocalTime time = deadline == null || deadline.isBlank() ? LocalTime.of(12, 0) : LocalTime.parse(deadline, TIME_FORMATTER);
        return LocalDateTime.of(date, time);
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

    private List<Long> requireLongList(List<String> values, String fieldName) {
        List<Long> parsed = parseLongList(values);
        if (parsed == null || parsed.isEmpty()) {
            throw new BusinessException(40001, fieldName + " is required");
        }
        return parsed;
    }

    private Long parseRequiredLong(String value, String fieldName) {
        Long parsed = parseLong(value);
        if (parsed == null) {
            throw new BusinessException(40001, fieldName + " is required");
        }
        return parsed;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank() || "ALL".equals(value) || "all".equals(value)) {
            return null;
        }
        return Long.valueOf(value);
    }

    private Long resolveCleanStaffId(Long campId, String cleanerId, String cleanStatus) {
        if (STATUS_PENDING_ASSIGN.equals(cleanStatus)) {
            return null;
        }
        Long cleanStaffId = parseLong(cleanerId);
        if (cleanStaffId == null) {
            return null;
        }
        if (cleanTaskMapper.countCleaner(campId, cleanStaffId) != 1) {
            throw new BusinessException(40401, "\u4FDD\u6D01\u5458\u4E0D\u5B58\u5728\u6216\u4E0D\u53EF\u7528");
        }
        return cleanStaffId;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        return resolveAccessibleCampId(requestedCampId, userId, "\u65E0\u6743\u8BBF\u95EE\u5F53\u524D\u95E8\u5E97\u4FDD\u6D01\u4EFB\u52A1");
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId, String forbiddenMessage) {
        Long currentCampId = userCampMapper.selectCurrentCampId(userId);
        if (currentCampId == null) {
            throw new BusinessException(40401, "\u672A\u627E\u5230\u5F53\u524D\u7528\u6237\u95E8\u5E97");
        }
        if (requestedCampId == null) {
            return currentCampId;
        }
        if (!requestedCampId.equals(currentCampId)) {
            throw new BusinessException(40301, forbiddenMessage);
        }
        return requestedCampId;
    }

    private LocalDateTime toLocalDateTime(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        return Instant.ofEpochMilli(epochMillis).atZone(SHANGHAI_ZONE).toLocalDateTime();
    }

    private List<CleanTaskQueryRowVO> selectStatisticsRows(
            Long campId,
            Long poiId,
            List<String> roomIds,
            List<String> cleanerIds,
            Long cleanStartTime,
            Long cleanEndTime
    ) {
        return cleanTaskMapper.selectTaskRowsByRange(
                campId,
                poiId,
                parseLongList(roomIds),
                parseLongList(cleanerIds),
                toLocalDateTime(cleanStartTime),
                toLocalDateTime(cleanEndTime)
        );
    }

    private List<CleanStatisticsDetailRowVO> toStatisticsDetailRows(List<CleanTaskQueryRowVO> rows) {
        return rows.stream()
                .map(this::toStatisticsDetailRow)
                .toList();
    }

    private CleanStatisticsDetailRowVO toStatisticsDetailRow(CleanTaskQueryRowVO row) {
        CleanStatisticsDetailRowVO detail = new CleanStatisticsDetailRowVO();
        detail.setId(row.getTaskId());
        detail.setCleanDate(row.getDeadlineAt() == null ? "" : row.getDeadlineAt().toLocalDate().format(DATE_FORMATTER));
        detail.setRoomName(row.getRoomCategoryName() + " / " + row.getRoomName());
        detail.setCleanerName(row.getCleanerName() == null || row.getCleanerName().isBlank() ? "待分配" : row.getCleanerName());
        detail.setCleanType(toCleanTypeLabel(row.getTaskType()));
        detail.setFee(resolveCleanFee(row.getTaskType()));
        detail.setStatus(toCleanStatusLabel(row.getTaskStatus()));
        return detail;
    }

    private List<CleanStatisticsMetricVO> buildStatisticsMetrics(List<CleanTaskQueryRowVO> rows) {
        long totalCount = rows.size();
        long totalFee = rows.stream().mapToLong(row -> resolveCleanFee(row.getTaskType())).sum();
        long doneCount = rows.stream().filter(row -> "done".equals(row.getTaskStatus())).count();
        long pendingCount = totalCount - doneCount;
        String passRate = totalCount == 0 ? "0" : String.format("%.1f", doneCount * 100.0 / totalCount);

        return List.of(
                new CleanStatisticsMetricVO("month-count", "本期保洁", String.valueOf(totalCount), "次", "按筛选条件实时统计", "当前筛选范围内的保洁任务数"),
                new CleanStatisticsMetricVO("month-fee", "保洁费用", formatMoney(totalFee), "元", "按真实任务类型计费", "当前筛选范围内的保洁费用合计"),
                new CleanStatisticsMetricVO("pass-rate", "完成率", passRate, "%", "已完成任务占比", "当前筛选范围内已完成任务占比"),
                new CleanStatisticsMetricVO("pending", "待处理", String.valueOf(pendingCount), "项", "需继续跟进", "待分配、待保洁、保洁中或已取消任务数")
        );
    }

    private List<CleanStatisticsTodoVO> buildStatisticsTodos(List<CleanTaskQueryRowVO> rows) {
        int checkoutCount = countByTaskType(rows, "checkout_clean");
        int pendingCount = (int) rows.stream().filter(row -> !"done".equals(row.getTaskStatus())).count();
        int cleanerCount = (int) rows.stream()
                .map(CleanTaskQueryRowVO::getCleanerId)
                .filter(cleanerId -> cleanerId != null && !cleanerId.isBlank())
                .distinct()
                .count();

        return List.of(
                new CleanStatisticsTodoVO("today-checkout", "退房保洁", checkoutCount, "查看房态"),
                new CleanStatisticsTodoVO("pending-acceptance", "待处理任务", pendingCount, "查看明细"),
                new CleanStatisticsTodoVO("staff-schedule", "保洁员排班", cleanerCount, "查看人员")
        );
    }

    private int countByTaskType(List<CleanTaskQueryRowVO> rows, String taskType) {
        return (int) rows.stream().filter(row -> taskType.equals(row.getTaskType())).count();
    }

    private String resolveExportDate(Long cleanStartTime) {
        LocalDateTime startTime = toLocalDateTime(cleanStartTime);
        if (startTime == null) {
            return LocalDate.now(SHANGHAI_ZONE).format(DATE_FORMATTER);
        }
        return startTime.toLocalDate().format(DATE_FORMATTER);
    }

    private String toCleanTypeLabel(String taskType) {
        return switch (taskType) {
            case "night_clean" -> "扫尘保洁";
            case "daily_clean" -> "续住保洁";
            case "checkout_clean" -> "退房保洁";
            default -> "深度保洁";
        };
    }

    private String toCleanStatusLabel(String taskStatus) {
        if ("done".equals(taskStatus)) {
            return "已完成";
        }
        if ("processing".equals(taskStatus)) {
            return "待验收";
        }
        return "已派单";
    }

    private long resolveCleanFee(String taskType) {
        return switch (taskType) {
            case "night_clean" -> FEE_TYPE_ONE;
            case "daily_clean" -> FEE_TYPE_TWO;
            case "checkout_clean" -> FEE_TYPE_THREE;
            default -> FEE_TYPE_FOUR;
        };
    }

    private String formatMoney(long cents) {
        return String.format("%.2f", cents / 100.0);
    }

    private List<CleanTaskStatisticsRowVO> aggregateStatisticsRows(List<CleanTaskQueryRowVO> rows) {
        Map<String, MutableStatisticsRow> grouped = new LinkedHashMap<>();
        for (CleanTaskQueryRowVO row : rows) {
            if (row.getDeadlineAt() == null) {
                continue;
            }
            String date = row.getDeadlineAt().toLocalDate().format(DATE_FORMATTER);
            grouped.computeIfAbsent(date, MutableStatisticsRow::new).accept(row.getTaskType());
        }

        List<CleanTaskStatisticsRowVO> result = new ArrayList<>();
        MutableStatisticsRow totalRow = new MutableStatisticsRow("合计");
        for (MutableStatisticsRow row : grouped.values()) {
            totalRow.merge(row);
        }
        result.add(totalRow.toView());
        grouped.values().forEach(row -> result.add(row.toView()));
        return result;
    }

    private <T> PageSlice<T> pageSlice(List<T> items, int pageNum, int pageSize) {
        long total = items.size();
        int fromIndex = Math.min((pageNum - 1) * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return new PageSlice<>(total, items.subList(fromIndex, toIndex));
    }

    private record PageSlice<T>(long total, List<T> items) {
    }

    private static final class MutableStatisticsRow {

        private final String cleanTime;
        private long countNum;
        private long countCost;
        private long cleanTypeOneNum;
        private long cleanTypeOneCost;
        private long cleanTypeTwoNum;
        private long cleanTypeTwoCost;
        private long cleanTypeThreeNum;
        private long cleanTypeThreeCost;
        private long cleanTypeFourNum;
        private long cleanTypeFourCost;

        private MutableStatisticsRow(String cleanTime) {
            this.cleanTime = cleanTime;
        }

        private void accept(String taskType) {
            countNum += 1;
            switch (taskType) {
                case "night_clean" -> {
                    cleanTypeOneNum += 1;
                    cleanTypeOneCost += FEE_TYPE_ONE;
                    countCost += FEE_TYPE_ONE;
                }
                case "daily_clean" -> {
                    cleanTypeTwoNum += 1;
                    cleanTypeTwoCost += FEE_TYPE_TWO;
                    countCost += FEE_TYPE_TWO;
                }
                case "checkout_clean" -> {
                    cleanTypeThreeNum += 1;
                    cleanTypeThreeCost += FEE_TYPE_THREE;
                    countCost += FEE_TYPE_THREE;
                }
                default -> {
                    cleanTypeFourNum += 1;
                    cleanTypeFourCost += FEE_TYPE_FOUR;
                    countCost += FEE_TYPE_FOUR;
                }
            }
        }

        private void merge(MutableStatisticsRow row) {
            countNum += row.countNum;
            countCost += row.countCost;
            cleanTypeOneNum += row.cleanTypeOneNum;
            cleanTypeOneCost += row.cleanTypeOneCost;
            cleanTypeTwoNum += row.cleanTypeTwoNum;
            cleanTypeTwoCost += row.cleanTypeTwoCost;
            cleanTypeThreeNum += row.cleanTypeThreeNum;
            cleanTypeThreeCost += row.cleanTypeThreeCost;
            cleanTypeFourNum += row.cleanTypeFourNum;
            cleanTypeFourCost += row.cleanTypeFourCost;
        }

        private CleanTaskStatisticsRowVO toView() {
            CleanTaskStatisticsRowVO row = new CleanTaskStatisticsRowVO();
            row.setCleanTime(cleanTime);
            row.setCountNum(countNum);
            row.setCountCost(countCost);
            row.setCleanTypeOneNum(cleanTypeOneNum);
            row.setCleanTypeOneCost(cleanTypeOneCost);
            row.setCleanTypeTwoNum(cleanTypeTwoNum);
            row.setCleanTypeTwoCost(cleanTypeTwoCost);
            row.setCleanTypeThreeNum(cleanTypeThreeNum);
            row.setCleanTypeThreeCost(cleanTypeThreeCost);
            row.setCleanTypeFourNum(cleanTypeFourNum);
            row.setCleanTypeFourCost(cleanTypeFourCost);
            return row;
        }
    }
}
