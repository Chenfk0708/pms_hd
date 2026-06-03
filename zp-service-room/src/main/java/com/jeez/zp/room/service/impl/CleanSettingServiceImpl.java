package com.jeez.zp.room.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.CleanSettingMapper;
import com.jeez.zp.room.mapper.UserCampMapper;
import com.jeez.zp.room.service.CleanSettingService;
import com.jeez.zp.room.vo.CleanSettingBootstrapResponseVO;
import com.jeez.zp.room.vo.CleanSettingExportResponseVO;
import com.jeez.zp.room.vo.CleanSettingMetricVO;
import com.jeez.zp.room.vo.CleanSettingOptionVO;
import com.jeez.zp.room.vo.CleanSettingPaginationVO;
import com.jeez.zp.room.vo.CleanSettingPolicyRuleVO;
import com.jeez.zp.room.vo.CleanSettingPriceRuleVO;
import com.jeez.zp.room.vo.CleanSettingReminderVO;
import com.jeez.zp.room.vo.CleanSettingRowVO;
import com.jeez.zp.room.vo.CleanSettingRuleSaveResponseVO;
import com.jeez.zp.room.vo.CleanSettingScheduleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CleanSettingServiceImpl implements CleanSettingService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String FILTER_ALL = "all";
    private static final String STATUS_ENABLED = "enabled";
    private static final String STATUS_PAUSED = "paused";
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final TypeReference<List<CleanSettingPolicyRuleVO>> POLICY_RULE_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<CleanSettingPriceRuleVO>> PRICE_RULE_LIST_TYPE = new TypeReference<>() {
    };

    private final CleanSettingMapper cleanSettingMapper;
    private final UserCampMapper userCampMapper;
    private final ObjectMapper objectMapper;

    @Override
    public CleanSettingBootstrapResponseVO bootstrap(
            Long campId,
            Long userId,
            String businessDate,
            String storeId,
            String projectId,
            String status,
            Integer page,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        LocalDate resolvedBusinessDate = parseBusinessDate(businessDate);
        Long resolvedStoreId = parseStoreId(storeId);
        int resolvedPage = normalizePage(page);
        int resolvedPageSize = normalizePageSize(pageSize);

        Map<String, CleanSettingRowVO> settingRows = cleanSettingMapper.selectSettings(resolvedCampId).stream()
                .collect(Collectors.toMap(CleanSettingRowVO::getConfigKey, Function.identity(), (left, right) -> right));
        List<CleanSettingPolicyRuleVO> policyRules = filterPolicyRules(
                parsePolicyRules(settingRows.get("policy_rules")),
                storeId,
                projectId,
                status
        );
        List<CleanSettingPriceRuleVO> priceRules = filterPriceRules(
                parsePriceRules(settingRows.get("price_rules")),
                projectId,
                status
        );
        PageSlice<CleanSettingPolicyRuleVO> policyPage = pageSlice(policyRules, resolvedPage, resolvedPageSize);
        long todayTasks = cleanSettingMapper.countActiveTasks(resolvedCampId, resolvedStoreId, resolvedBusinessDate);
        long pendingTasks = cleanSettingMapper.countPendingTasks(resolvedCampId, resolvedStoreId, resolvedBusinessDate);

        CleanSettingBootstrapResponseVO response = new CleanSettingBootstrapResponseVO();
        response.setStores(buildStores(resolvedCampId));
        response.setProjects(buildProjects());
        response.setStatusOptions(buildStatusOptions());
        response.setMetrics(buildMetrics(todayTasks, countEnabled(policyRules)));
        response.setPolicyRules(policyPage.items());
        response.setPriceRules(priceRules);
        response.setReminders(buildReminders(pendingTasks));
        response.setSchedule(buildSchedule(resolvedCampId, resolvedStoreId, resolvedBusinessDate));
        response.setPagination(toPagination(policyPage.total(), resolvedPage, resolvedPageSize));
        response.setRequestedAt(LocalDateTime.now(SHANGHAI_ZONE).format(DATE_TIME_FORMATTER));
        return response;
    }

    @Override
    @Transactional
    public CleanSettingRuleSaveResponseVO savePolicyRule(Long campId, Long userId, CleanSettingPolicyRuleVO rule) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        validateRule(rule);

        List<CleanSettingPolicyRuleVO> rules = new ArrayList<>(loadPolicyRules(resolvedCampId));
        rules.removeIf(existing -> rule.getId().equals(existing.getId()));
        if (rule.getUpdatedAt() == null || rule.getUpdatedAt().isBlank()) {
            rule.setUpdatedAt(LocalDateTime.now(SHANGHAI_ZONE).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
        rules.add(rule);
        rules.sort(Comparator.comparing(CleanSettingPolicyRuleVO::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        cleanSettingMapper.upsertSetting(
                IdWorker.getId(),
                resolvedCampId,
                "policy_rules",
                writeJson(rules, "\u4FDD\u6D01\u7B56\u7565\u914D\u7F6E\u683C\u5F0F\u9519\u8BEF"),
                userId
        );

        CleanSettingRuleSaveResponseVO response = new CleanSettingRuleSaveResponseVO();
        response.setRule(rule);
        response.setTotal(rules.size());
        response.setMessage("\u4FDD\u6D01\u7B56\u7565\u4FDD\u5B58\u6210\u529F");
        return response;
    }

    @Override
    public CleanSettingExportResponseVO export(
            Long campId,
            Long userId,
            String businessDate,
            String storeId,
            String projectId,
            String status
    ) {
        CleanSettingBootstrapResponseVO dashboard = bootstrap(
                campId,
                userId,
                businessDate,
                storeId,
                projectId,
                status,
                DEFAULT_PAGE,
                Integer.MAX_VALUE
        );
        LocalDate exportDate = parseBusinessDate(businessDate);
        CleanSettingExportResponseVO response = new CleanSettingExportResponseVO();
        response.setFileName("clean_setting_" + exportDate.format(DATE_FORMATTER) + ".csv");
        response.setContentType("text/csv");
        response.setPolicyRules(dashboard.getPolicyRules());
        response.setPriceRules(dashboard.getPriceRules());
        response.setTotal(dashboard.getPolicyRules().size() + dashboard.getPriceRules().size());
        return response;
    }

    private List<CleanSettingOptionVO> buildStores(Long campId) {
        List<CleanSettingOptionVO> stores = new ArrayList<>();
        stores.add(new CleanSettingOptionVO(FILTER_ALL, "\u5168\u90E8\u95E8\u5E97"));
        stores.addAll(cleanSettingMapper.selectStores(campId));
        return stores;
    }

    private List<CleanSettingOptionVO> buildProjects() {
        return List.of(
                new CleanSettingOptionVO(FILTER_ALL, "\u5168\u90E8\u9879\u76EE"),
                new CleanSettingOptionVO("daily-clean", "\u65E5\u5E38\u4FDD\u6D01"),
                new CleanSettingOptionVO("deep-clean", "\u6DF1\u5EA6\u4FDD\u6D01")
        );
    }

    private List<CleanSettingOptionVO> buildStatusOptions() {
        return List.of(
                new CleanSettingOptionVO(FILTER_ALL, "\u5168\u90E8\u72B6\u6001"),
                new CleanSettingOptionVO(STATUS_ENABLED, "\u5DF2\u542F\u7528"),
                new CleanSettingOptionVO(STATUS_PAUSED, "\u5DF2\u6682\u505C")
        );
    }

    private List<CleanSettingMetricVO> buildMetrics(long todayTasks, long enabledRules) {
        return List.of(
                new CleanSettingMetricVO("todayTasks", "\u4ECA\u65E5\u4EFB\u52A1", String.valueOf(todayTasks), "\u6309\u5F53\u524D\u6761\u4EF6\u7EDF\u8BA1\u5F85\u6267\u884C\u4E0E\u6267\u884C\u4E2D\u4EFB\u52A1"),
                new CleanSettingMetricVO("enabledRules", "\u542F\u7528\u7B56\u7565", String.valueOf(enabledRules), "\u53EF\u81EA\u52A8\u89E6\u53D1\u7684\u4FDD\u6D01\u7B56\u7565"),
                new CleanSettingMetricVO("avgResponse", "\u5E73\u5747\u63A5\u5355", "-", "\u4FDD\u6D01\u5458\u63A5\u5355\u5E73\u5747\u54CD\u5E94\u65F6\u95F4"),
                new CleanSettingMetricVO("exceptionRate", "\u5F02\u5E38\u7387", "-", "\u8D85\u65F6\u3001\u9000\u56DE\u548C\u4EBA\u5DE5\u6539\u6D3E\u5360\u6BD4")
        );
    }

    private List<CleanSettingReminderVO> buildReminders(long pendingTasks) {
        if (pendingTasks < 1) {
            return Collections.emptyList();
        }
        return List.of(new CleanSettingReminderVO(
                "pending-clean-task",
                "\u5F85\u5904\u7406\u4FDD\u6D01\u4EFB\u52A1",
                pendingTasks + " \u6761\u4FDD\u6D01\u4EFB\u52A1\u5F85\u63A5\u5355\u6216\u5F85\u6267\u884C",
                pendingTasks > 5 ? "warning" : "normal"
        ));
    }

    private List<CleanSettingScheduleVO> buildSchedule(Long campId, Long storeId, LocalDate businessDate) {
        return List.of(
                new CleanSettingScheduleVO("09:00-12:00", formatTaskCount(campId, storeId, businessDate, 9, 12), "primary"),
                new CleanSettingScheduleVO("12:00-16:00", formatTaskCount(campId, storeId, businessDate, 12, 16), "success"),
                new CleanSettingScheduleVO("16:00-20:00", formatTaskCount(campId, storeId, businessDate, 16, 20), "warning")
        );
    }

    private String formatTaskCount(Long campId, Long storeId, LocalDate businessDate, int startHour, int endHour) {
        long activeTasks = cleanSettingMapper.countActiveTasksByHourRange(campId, storeId, businessDate, startHour, endHour);
        return activeTasks + " \u95F4";
    }

    private List<CleanSettingPolicyRuleVO> parsePolicyRules(CleanSettingRowVO row) {
        if (row == null || row.getConfigValue() == null || row.getConfigValue().isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<CleanSettingPolicyRuleVO> rules = objectMapper.readValue(row.getConfigValue(), POLICY_RULE_LIST_TYPE);
            return rules.stream()
                    .filter(rule -> rule.getId() != null && !rule.getId().isBlank())
                    .sorted(Comparator.comparing(CleanSettingPolicyRuleVO::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
        } catch (Exception ex) {
            throw new BusinessException(50001, "\u4FDD\u6D01\u7B56\u7565\u914D\u7F6E\u683C\u5F0F\u9519\u8BEF");
        }
    }

    private List<CleanSettingPolicyRuleVO> loadPolicyRules(Long campId) {
        Map<String, CleanSettingRowVO> settingRows = cleanSettingMapper.selectSettings(campId).stream()
                .collect(Collectors.toMap(CleanSettingRowVO::getConfigKey, Function.identity(), (left, right) -> right));
        return parsePolicyRules(settingRows.get("policy_rules"));
    }

    private String writeJson(Object value, String errorMessage) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BusinessException(50001, errorMessage);
        }
    }

    private void validateRule(CleanSettingPolicyRuleVO rule) {
        if (rule == null) {
            throw new BusinessException(40001, "rule is required");
        }
        requireText(rule.getId(), "rule.id");
        requireText(rule.getName(), "rule.name");
        if (!STATUS_ENABLED.equals(rule.getStatus()) && !STATUS_PAUSED.equals(rule.getStatus())) {
            throw new BusinessException(40001, "rule.status is invalid");
        }
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(40001, fieldName + " is required");
        }
    }

    private List<CleanSettingPriceRuleVO> parsePriceRules(CleanSettingRowVO row) {
        if (row == null || row.getConfigValue() == null || row.getConfigValue().isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<CleanSettingPriceRuleVO> rules = objectMapper.readValue(row.getConfigValue(), PRICE_RULE_LIST_TYPE);
            return rules.stream()
                    .filter(rule -> rule.getId() != null && !rule.getId().isBlank())
                    .toList();
        } catch (Exception ex) {
            throw new BusinessException(50001, "\u4FDD\u6D01\u4EF7\u683C\u914D\u7F6E\u683C\u5F0F\u9519\u8BEF");
        }
    }

    private List<CleanSettingPolicyRuleVO> filterPolicyRules(
            List<CleanSettingPolicyRuleVO> rules,
            String storeId,
            String projectId,
            String status
    ) {
        return rules.stream()
                .filter(rule -> matchesFilter(storeId, rule.getStoreId()))
                .filter(rule -> matchesFilter(projectId, rule.getProjectId()))
                .filter(rule -> matchesFilter(status, rule.getStatus()))
                .toList();
    }

    private List<CleanSettingPriceRuleVO> filterPriceRules(
            List<CleanSettingPriceRuleVO> rules,
            String projectId,
            String status
    ) {
        return rules.stream()
                .filter(rule -> matchesFilter(projectId, rule.getProjectId()))
                .filter(rule -> matchesFilter(status, rule.getStatus()))
                .toList();
    }

    private boolean matchesFilter(String requestedValue, String actualValue) {
        return requestedValue == null
                || requestedValue.isBlank()
                || FILTER_ALL.equals(requestedValue)
                || requestedValue.equals(actualValue);
    }

    private long countEnabled(List<CleanSettingPolicyRuleVO> policyRules) {
        return policyRules.stream()
                .filter(rule -> STATUS_ENABLED.equals(rule.getStatus()))
                .count();
    }

    private LocalDate parseBusinessDate(String businessDate) {
        if (businessDate == null || businessDate.isBlank()) {
            return LocalDate.now(SHANGHAI_ZONE);
        }
        return LocalDate.parse(businessDate, DATE_FORMATTER);
    }

    private Long parseStoreId(String storeId) {
        if (storeId == null || storeId.isBlank() || FILTER_ALL.equals(storeId)) {
            return null;
        }
        return Long.valueOf(storeId);
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private CleanSettingPaginationVO toPagination(long total, int page, int pageSize) {
        CleanSettingPaginationVO pagination = new CleanSettingPaginationVO();
        pagination.setPage(page);
        pagination.setPageSize(pageSize);
        pagination.setTotal(total);
        return pagination;
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
            throw new BusinessException(40301, "\u65E0\u6743\u8BBF\u95EE\u5F53\u524D\u95E8\u5E97\u4FDD\u6D01\u8BBE\u7F6E");
        }
        return requestedCampId;
    }

    private <T> PageSlice<T> pageSlice(List<T> items, int page, int pageSize) {
        long total = items.size();
        int fromIndex = Math.min((page - 1) * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return new PageSlice<>(total, items.subList(fromIndex, toIndex));
    }

    private record PageSlice<T>(long total, List<T> items) {
    }
}
