package com.jeez.zp.room.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.jeez.zp.room.dto.request.CleanerPageRequest;
import com.jeez.zp.room.dto.request.CleanerSaveRequest;
import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.CleanTaskMapper;
import com.jeez.zp.room.mapper.UserCampMapper;
import com.jeez.zp.room.service.CleanerService;
import com.jeez.zp.room.vo.CleanTaskOptionVO;
import com.jeez.zp.room.vo.CleanerExportResponseVO;
import com.jeez.zp.room.vo.CleanerListItemVO;
import com.jeez.zp.room.vo.CleanerPageItemVO;
import com.jeez.zp.room.vo.CleanerPagePaginationVO;
import com.jeez.zp.room.vo.CleanerPageQueryRowVO;
import com.jeez.zp.room.vo.CleanerPageResponseVO;
import com.jeez.zp.room.vo.CleanerPageStoreVO;
import com.jeez.zp.room.vo.CleanerPageSummaryVO;
import com.jeez.zp.room.vo.CleanerSaveResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CleanerServiceImpl implements CleanerService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String STATUS_ALL = "all";
    private static final String STATUS_ON_DUTY = "onDuty";
    private static final String STATUS_OFF_DUTY = "offDuty";
    private static final String STATUS_LEAVE = "leave";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String MOBILE_PATTERN = "^1[3-9]\\d{9}$";

    private final CleanTaskMapper cleanTaskMapper;
    private final UserCampMapper userCampMapper;

    @Override
    public List<CleanerListItemVO> getCleaners(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        return cleanTaskMapper.selectCleaners(resolvedCampId).stream()
                .map(this::toCleaner)
                .toList();
    }

    @Override
    public CleanerPageResponseVO getPage(CleanerPageRequest request, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(parseLong(request.getCampId()), userId);
        Long poiId = parseLong(request.getPoiId());
        String keyword = normalizeText(request.getKeyword());
        String status = normalizeStatus(request.getStatus());
        LocalDate serviceDate = parseDate(request.getServiceDate());
        int pageNum = normalizePageNum(request.getPageNum());
        int pageSize = normalizePageSize(request.getPageSize());

        List<CleanerPageItemVO> filteredItems = cleanTaskMapper
                .selectCleanerPageRows(resolvedCampId, poiId, keyword, serviceDate)
                .stream()
                .map(this::toPageItem)
                .filter(item -> matchesStatus(item, status))
                .toList();

        PageSlice<CleanerPageItemVO> pageSlice = pageSlice(filteredItems, pageNum, pageSize);

        CleanerPageResponseVO response = new CleanerPageResponseVO();
        response.setStores(buildStores(resolvedCampId));
        response.setSummary(buildSummary(filteredItems));
        response.setList(pageSlice.items());
        response.setPagination(toPagination(pageSlice.total(), pageNum, pageSize));
        response.setRequestBody(buildRequestBody(resolvedCampId, poiId, keyword, status, request.getServiceDate(), pageNum, pageSize));
        return response;
    }

    @Override
    public CleanerSaveResponseVO save(CleanerSaveRequest request, Long userId) {
        if (request == null) {
            throw new BusinessException(40001, "request is required");
        }
        Long resolvedCampId = resolveAccessibleCampId(parseLong(request.getCampId()), userId);
        String name = requireText(request.getName(), "name");
        String mobile = requireMobile(request.getMobile());
        String status = normalizeStatus(request.getStatus());
        Integer rawStatus = STATUS_OFF_DUTY.equals(status) || STATUS_LEAVE.equals(status) ? 0 : 1;
        Long cleanerId = IdWorker.getId();

        cleanTaskMapper.insertCleaner(
                cleanerId,
                resolvedCampId,
                name,
                mobile,
                rawStatus,
                trimToNull(request.getRoomScopeText())
        );

        CleanerSaveResponseVO response = new CleanerSaveResponseVO();
        response.setSaved(true);
        response.setCleanerId(String.valueOf(cleanerId));
        return response;
    }

    @Override
    public CleanerExportResponseVO export(CleanerPageRequest request, Long userId) {
        CleanerPageRequest exportRequest = new CleanerPageRequest();
        exportRequest.setCampId(request == null ? null : request.getCampId());
        exportRequest.setPoiId(request == null ? null : request.getPoiId());
        exportRequest.setKeyword(request == null ? null : request.getKeyword());
        exportRequest.setStatus(request == null ? null : request.getStatus());
        exportRequest.setServiceDate(request == null ? null : request.getServiceDate());
        exportRequest.setPageNum(DEFAULT_PAGE_NUM);
        exportRequest.setPageSize(Integer.MAX_VALUE);

        CleanerPageResponseVO page = getPage(exportRequest, userId);
        String exportedDate = exportRequest.getServiceDate() == null || exportRequest.getServiceDate().isBlank()
                ? LocalDate.now().format(DATE_FORMATTER)
                : exportRequest.getServiceDate();

        CleanerExportResponseVO response = new CleanerExportResponseVO();
        response.setTaskId("CLEANER-EXPORT-" + exportedDate.replace("-", ""));
        response.setFileName("cleaners_" + exportedDate.replace("-", "") + ".csv");
        response.setContentType("text/csv");
        response.setTotal(page.getList().size());
        response.setRows(page.getList());
        return response;
    }

    private CleanerListItemVO toCleaner(CleanTaskOptionVO option) {
        CleanerListItemVO cleaner = new CleanerListItemVO();
        cleaner.setCleanerId(option.getId());
        cleaner.setCleanerName(option.getLabel());
        return cleaner;
    }

    private CleanerPageItemVO toPageItem(CleanerPageQueryRowVO row) {
        String workStatus = row.getRawStatus() != null && row.getRawStatus() == 1 ? STATUS_ON_DUTY : STATUS_OFF_DUTY;
        int todayTaskNum = nonNull(row.getTodayTaskNum());
        int completedTaskNum = nonNull(row.getCompletedTaskNum());
        int overdueTaskNum = nonNull(row.getOverdueTaskNum());
        List<String> roomScopes = splitCsv(row.getRoomScopesText());

        CleanerPageItemVO item = new CleanerPageItemVO();
        item.setId(row.getCleanerId());
        item.setCleanerId(row.getCleanerId());
        item.setName(row.getCleanerName());
        item.setCleanerName(row.getCleanerName());
        item.setMobile(row.getMobile());
        item.setPoiId(emptyToNull(row.getPoiId()));
        item.setPoiName(blankToDefault(row.getPoiName(), "-"));
        item.setStoreName(item.getPoiName());
        item.setWorkStatus(workStatus);
        item.setStatus(workStatus);
        item.setStatusText(statusText(workStatus));
        item.setRoleName("保洁员");
        item.setRole("保洁员");
        item.setRoomScopes(roomScopes);
        item.setRoomScope(roomScopes);
        item.setTodayTaskNum(todayTaskNum);
        item.setTodayTasks(todayTaskNum);
        item.setCompletedTaskNum(completedTaskNum);
        item.setCompletedTasks(completedTaskNum);
        item.setOverdueTaskNum(overdueTaskNum);
        item.setOverdueTasks(overdueTaskNum);
        item.setServiceScore(100);
        item.setRating("100%");
        item.setLastTaskTime(blankToDefault(row.getLastTaskTime(), "-"));
        item.setLastTaskAt(item.getLastTaskTime());
        return item;
    }

    private List<CleanerPageStoreVO> buildStores(Long campId) {
        CleanerPageStoreVO all = new CleanerPageStoreVO();
        all.setId("all");
        all.setName("全部门店");

        List<CleanerPageStoreVO> stores = cleanTaskMapper.selectStores(campId).stream()
                .map(this::toStore)
                .toList();

        return java.util.stream.Stream.concat(java.util.stream.Stream.of(all), stores.stream()).toList();
    }

    private CleanerPageStoreVO toStore(CleanTaskOptionVO option) {
        CleanerPageStoreVO store = new CleanerPageStoreVO();
        store.setId(option.getId());
        store.setName(option.getLabel());
        return store;
    }

    private CleanerPageSummaryVO buildSummary(List<CleanerPageItemVO> items) {
        CleanerPageSummaryVO summary = new CleanerPageSummaryVO();
        summary.setTotal(items.size());
        summary.setOnDuty((int) items.stream().filter(item -> STATUS_ON_DUTY.equals(item.getWorkStatus())).count());
        summary.setOffDuty((int) items.stream().filter(item -> STATUS_OFF_DUTY.equals(item.getWorkStatus())).count());
        summary.setLeave((int) items.stream().filter(item -> STATUS_LEAVE.equals(item.getWorkStatus())).count());
        summary.setTodayTasks(items.stream().mapToInt(CleanerPageItemVO::getTodayTaskNum).sum());
        summary.setCompletedTasks(items.stream().mapToInt(CleanerPageItemVO::getCompletedTaskNum).sum());
        summary.setOverdueTasks(items.stream().mapToInt(CleanerPageItemVO::getOverdueTaskNum).sum());
        return summary;
    }

    private CleanerPagePaginationVO toPagination(long total, int pageNum, int pageSize) {
        CleanerPagePaginationVO pagination = new CleanerPagePaginationVO();
        pagination.setPage(pageNum);
        pagination.setPageNum(pageNum);
        pagination.setPageSize(pageSize);
        pagination.setTotal(total);
        return pagination;
    }

    private Map<String, Object> buildRequestBody(
            Long campId,
            Long poiId,
            String keyword,
            String status,
            String serviceDate,
            int pageNum,
            int pageSize
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("campId", String.valueOf(campId));
        body.put("poiId", poiId == null ? "" : String.valueOf(poiId));
        body.put("keyword", keyword == null ? "" : keyword);
        body.put("status", STATUS_ALL.equals(status) ? "" : status);
        body.put("serviceDate", serviceDate == null ? "" : serviceDate);
        body.put("pageNum", pageNum);
        body.put("pageSize", pageSize);
        return body;
    }

    private boolean matchesStatus(CleanerPageItemVO item, String status) {
        return status == null || STATUS_ALL.equals(status) || status.equals(item.getWorkStatus());
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
            throw new BusinessException(40301, "\u65E0\u6743\u8BBF\u95EE\u5F53\u524D\u95E8\u5E97\u4FDD\u6D01\u7EDF\u8BA1");
        }
        return requestedCampId;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank() || "all".equalsIgnoreCase(value) || "ALL".equals(value)) {
            return null;
        }
        return Long.valueOf(value);
    }

    private String normalizeText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String requireText(String value, String fieldName) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            throw new BusinessException(40001, fieldName + " is required");
        }
        return normalized;
    }

    private String requireMobile(String value) {
        String mobile = requireText(value, "mobile");
        if (!mobile.matches(MOBILE_PATTERN)) {
            throw new BusinessException(40001, "\u624B\u673A\u53F7\u683C\u5F0F\u4E0D\u6B63\u786E");
        }
        return mobile;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return STATUS_ALL;
        }
        return status;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value, DATE_FORMATTER);
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private int nonNull(Integer value) {
        return value == null ? 0 : value;
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String statusText(String status) {
        if (STATUS_ON_DUTY.equals(status)) {
            return "在岗";
        }
        if (STATUS_OFF_DUTY.equals(status)) {
            return "休息";
        }
        return "请假";
    }

    private <T> PageSlice<T> pageSlice(List<T> items, int pageNum, int pageSize) {
        int fromIndex = Math.min((pageNum - 1) * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return new PageSlice<>(items.size(), items.subList(fromIndex, toIndex));
    }

    private record PageSlice<T>(long total, List<T> items) {
    }
}
