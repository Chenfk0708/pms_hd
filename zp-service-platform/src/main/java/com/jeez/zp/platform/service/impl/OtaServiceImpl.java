package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.OtaChannelDetailRequest;
import com.jeez.zp.platform.dto.request.OtaDashboardRequest;
import com.jeez.zp.platform.dto.request.OtaLogPageRequest;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.OtaMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.OtaService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.OtaAccountQueryRowVO;
import com.jeez.zp.platform.vo.OtaChannelAuthorizationNoticeVO;
import com.jeez.zp.platform.vo.OtaChannelDetailVO;
import com.jeez.zp.platform.vo.OtaChannelNoticeSectionVO;
import com.jeez.zp.platform.vo.OtaChannelVO;
import com.jeez.zp.platform.vo.OtaDashboardRequestVO;
import com.jeez.zp.platform.vo.OtaDashboardVO;
import com.jeez.zp.platform.vo.OtaDetailRoomQueryRowVO;
import com.jeez.zp.platform.vo.OtaDetailRoomRowVO;
import com.jeez.zp.platform.vo.OtaDetailStoreQueryRowVO;
import com.jeez.zp.platform.vo.OtaDetailStoreRowVO;
import com.jeez.zp.platform.vo.OtaLogPageVO;
import com.jeez.zp.platform.vo.OtaLogPaginationVO;
import com.jeez.zp.platform.vo.OtaLogQueryRowVO;
import com.jeez.zp.platform.vo.OtaLogRowVO;
import com.jeez.zp.platform.vo.OtaMetricVO;
import com.jeez.zp.platform.vo.OtaOptionVO;
import com.jeez.zp.platform.vo.OtaQuickLinkVO;
import com.jeez.zp.platform.vo.OtaReminderVO;
import com.jeez.zp.platform.vo.OtaSyncStoreDefaultsVO;
import com.jeez.zp.platform.vo.OtaSyncStoreNoticeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OtaServiceImpl implements OtaService {

    private static final String AUTHORIZED_STATUS = "authorized";
    private static final String PENDING_STATUS = "pending";
    private static final String CONNECTED_DIMENSION = "connected";
    private static final String PENDING_DIMENSION = "pending";
    private static final String DEFAULT_DIMENSION = "all";
    private static final String DEFAULT_STORE = "all";
    private static final String DEFAULT_FILTER_VALUE = "all";
    private static final String OPERATION_STATUS_SUCCESS = "success";
    private static final String OPERATION_STATUS_FAILED = "failed";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Map<Long, ChannelProfile> CHANNEL_PROFILES = buildChannelProfiles();

    private final OtaMapper otaMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public OtaDashboardVO getDashboard(OtaDashboardRequest request, Long userId) {
        Long campId = resolveAccessibleCampId(parseNullableLong(request.getCampId()), userId);
        String businessDate = normalizeBusinessDate(request.getBusinessDate());
        String storeId = normalizeStoreId(request.getStoreId());
        String dimension = normalizeDimension(request.getDimension());
        Long poiId = DEFAULT_STORE.equals(storeId) ? null : parseNullableLong(storeId);

        List<OtaAccountQueryRowVO> accountRows = otaMapper.selectAccounts(campId, poiId);
        List<OtaChannelVO> connectedChannels = accountRows.stream()
                .filter(row -> isAuthorized(row.getStatus()))
                .map(row -> toChannel(row, "connected"))
                .toList();
        List<OtaChannelVO> pendingChannels = accountRows.stream()
                .filter(row -> !isAuthorized(row.getStatus()))
                .map(row -> toChannel(row, "pending"))
                .toList();

        List<OtaChannelVO> visibleConnected = PENDING_DIMENSION.equals(dimension) ? List.of() : connectedChannels;
        List<OtaChannelVO> visiblePending = CONNECTED_DIMENSION.equals(dimension) ? List.of() : pendingChannels;
        int roomTypeCount = accountRows.stream()
                .map(OtaAccountQueryRowVO::getRoomTypeCount)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(0);
        int mappedRoomTypeCount = connectedChannels.stream()
                .map(OtaChannelVO::getMappedRoomTypeCount)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        OtaDashboardVO dashboard = new OtaDashboardVO();
        dashboard.setStores(storeOptions(campId));
        dashboard.setDimensions(dimensionOptions());
        dashboard.setMetrics(metrics(visibleConnected.size(), visiblePending.size(), mappedRoomTypeCount, roomTypeCount, latestSyncTime(connectedChannels)));
        dashboard.setConnectedChannels(visibleConnected);
        dashboard.setPendingChannels(visiblePending);
        dashboard.setReminders(reminders(visiblePending.size()));
        dashboard.setQuickLinks(quickLinks());
        dashboard.setUpdatedAt(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        dashboard.setProvider("api");
        dashboard.setTraceId(TraceIdFactory.next("ota-dashboard"));
        dashboard.setRequest(requestVO(businessDate, storeId, dimension));
        return dashboard;
    }

    @Override
    public OtaChannelDetailVO getChannelDetail(OtaChannelDetailRequest request, Long userId) {
        Long campId = resolveAccessibleCampId(parseNullableLong(request.getCampId()), userId);
        String requestedChannelId = normalizeChannelKey(request.getChannelId());
        Long channelId = channelKeyToId(requestedChannelId);
        if (channelId == null) {
            throw new BusinessException(40404, "OTA渠道不存在");
        }

        List<OtaAccountQueryRowVO> channelAccounts = otaMapper.selectAccounts(campId, null).stream()
                .filter(row -> channelId.equals(row.getChannelId()))
                .toList();
        if (channelAccounts.isEmpty()) {
            throw new BusinessException(40404, "OTA渠道不存在");
        }
        List<Long> accountIds = channelAccounts.stream()
                .map(OtaAccountQueryRowVO::getAccountIdRaw)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        OtaAccountQueryRowVO primaryAccount = channelAccounts.get(0);
        ChannelProfile profile = profile(primaryAccount.getChannelId(), primaryAccount.getChannelName());

        OtaChannelDetailVO detail = new OtaChannelDetailVO();
        detail.setId(profile.key());
        detail.setChannelName(profile.displayName());
        detail.setTitle(profile.displayName());
        detail.setDescription(profile.displayName() + "已接入，可在下方门店管理添加渠道门店，或进行同步房型操作。");
        detail.setLogoText(profile.logoText());
        detail.setLogoTone(profile.logoTone());
        detail.setNoticeText(profile.noticeText());
        detail.setNoticeLinkLabel(profile.noticeLinkLabel());
        detail.setChannelStoreOptions(withAll(otaMapper.selectDetailStoreRows(campId, accountIds).stream()
                .map(this::toStoreOption)
                .distinct()
                .toList(), "全部"));
        detail.setAccountOptions(withAll(channelAccounts.stream()
                .map(this::toAccountOption)
                .toList(), "全部"));
        detail.setStatusOptions(statusOptions());
        detail.setRoomRows(otaMapper.selectDetailRoomRows(campId, accountIds).stream().map(this::toRoomRow).toList());
        detail.setStoreRows(otaMapper.selectDetailStoreRows(campId, accountIds).stream().map(this::toStoreRow).toList());
        detail.setSyncStoreNotice(syncStoreNotice(profile.displayName()));
        detail.setSyncStoreDefaults(syncStoreDefaults());
        return detail;
    }

    @Override
    public OtaLogPageVO getLogPage(OtaLogPageRequest request, Long userId) {
        Long campId = resolveAccessibleCampId(parseNullableLong(request.getCampId()), userId);
        int page = normalizePage(request.getPage());
        int pageSize = normalizePageSize(request.getPageSize());
        String channelId = normalizeLogFilterValue(request.getChannelId());
        String operationType = normalizeLogFilterValue(request.getOperationType());
        String operationStatus = normalizeLogFilterValue(request.getOperationStatus());
        String keyword = trimToNull(request.getKeyword());
        String operator = trimToNull(request.getOperator());

        List<OtaLogRowVO> filteredRows = otaMapper.selectLogRows(campId).stream()
                .map(this::toOtaLogRow)
                .filter(row -> matchesLogRow(row, channelId, operationType, operationStatus, keyword, operator))
                .toList();

        long total = filteredRows.size();
        int fromIndex = Math.min((page - 1) * pageSize, filteredRows.size());
        int toIndex = Math.min(fromIndex + pageSize, filteredRows.size());

        OtaLogPaginationVO pagination = new OtaLogPaginationVO();
        pagination.setPage(page);
        pagination.setPageSize(pageSize);
        pagination.setTotal(total);

        OtaLogPageVO response = new OtaLogPageVO();
        response.setChannelOptions(buildChannelOptions(campId));
        response.setOperationTypeOptions(operationTypeOptions());
        response.setOperationStatusOptions(operationStatusOptions());
        response.setRows(filteredRows.subList(fromIndex, toIndex));
        response.setPagination(pagination);
        return response;
    }

    private List<OtaOptionVO> storeOptions(Long campId) {
        return withAll(otaMapper.selectStoreOptions(campId), "全部门店");
    }

    private List<OtaOptionVO> withAll(List<OtaOptionVO> options, String allLabel) {
        Map<String, OtaOptionVO> deduplicated = new LinkedHashMap<>();
        deduplicated.put("all", option("all", allLabel));
        for (OtaOptionVO option : options) {
            if (option == null || option.getValue() == null || option.getValue().isBlank()) {
                continue;
            }
            deduplicated.putIfAbsent(option.getValue(), option);
        }
        return deduplicated.values().stream().toList();
    }

    private List<OtaOptionVO> dimensionOptions() {
        return List.of(
                option("all", "全部渠道"),
                option("connected", "已直连"),
                option("pending", "未直连")
        );
    }

    private List<OtaOptionVO> statusOptions() {
        return List.of(
                option("all", "全部"),
                option("linked", "已关联"),
                option("unlinked", "未关联")
        );
    }

    private List<OtaOptionVO> operationTypeOptions() {
        return List.of(
                option(DEFAULT_FILTER_VALUE, "全部类型"),
                option("bindRoomType", "关联渠道房型"),
                option("unbindRoomType", "解除渠道房型"),
                option("bindAccount", "渠道授权")
        );
    }

    private List<OtaOptionVO> operationStatusOptions() {
        return List.of(
                option(DEFAULT_FILTER_VALUE, "全部状态"),
                option(OPERATION_STATUS_SUCCESS, "成功"),
                option(OPERATION_STATUS_FAILED, "失败")
        );
    }

    private List<OtaMetricVO> metrics(int connectedCount, int pendingCount, int mappedRoomTypeCount, int roomTypeCount, String latestSyncTime) {
        return List.of(
                metric("connected", "已直连", String.valueOf(connectedCount), "可同步房型、价格、库存"),
                metric("pending", "未直连", String.valueOf(pendingCount), "可发起授权或渠道申请"),
                metric("roomTypes", "关联房型", mappedRoomTypeCount + "/" + roomTypeCount, "当前渠道房型映射进度"),
                metric("sync", "最近同步", latestSyncTime, "库存、房价、订单状态同步时间")
        );
    }

    private OtaMetricVO metric(String key, String label, String value, String detail) {
        OtaMetricVO metric = new OtaMetricVO();
        metric.setKey(key);
        metric.setLabel(label);
        metric.setValue(value);
        metric.setDetail(detail);
        return metric;
    }

    private List<OtaReminderVO> reminders(int pendingCount) {
        if (pendingCount <= 0) {
            return List.of();
        }
        OtaReminderVO reminder = new OtaReminderVO();
        reminder.setId("pending-channel");
        reminder.setTitle("待授权渠道");
        reminder.setDetail("当前还有 " + pendingCount + " 个渠道待完成授权。");
        return List.of(reminder);
    }

    private List<OtaQuickLinkVO> quickLinks() {
        OtaQuickLinkVO logLink = new OtaQuickLinkVO();
        logLink.setId("operation-log");
        logLink.setLabel("操作日志");
        logLink.setRoute("/channels/ota/log");
        return List.of(logLink);
    }

    private OtaChannelVO toChannel(OtaAccountQueryRowVO row, String connectionStatus) {
        ChannelProfile profile = profile(row.getChannelId(), row.getChannelName());
        int roomTypeCount = safeInt(row.getRoomTypeCount());
        int mappedRoomTypeCount = "connected".equals(connectionStatus) ? safeInt(row.getMappedRoomTypeCount()) : 0;

        OtaChannelVO channel = new OtaChannelVO();
        channel.setId(profile.key());
        channel.setAccountId(row.getAccountId());
        channel.setName(profile.displayName());
        channel.setRelation("connected".equals(connectionStatus) ? "关联房型 " + mappedRoomTypeCount + "/" + roomTypeCount : "等待授权");
        channel.setStatus(connectionStatus);
        channel.setRoomTypeCount(roomTypeCount);
        channel.setMappedRoomTypeCount(mappedRoomTypeCount);
        channel.setLastSyncAt("connected".equals(connectionStatus) ? formatDateTime(resolveLastSyncAt(row)) : "-");
        channel.setLogoText(profile.logoText());
        channel.setDetail("connected".equals(connectionStatus)
                ? profile.displayName() + " 已完成房型、价格、库存同步"
                : profile.displayName() + " 可发起渠道授权申请");
        channel.setAuthorizationNotice(authorizationNotice(profile, connectionStatus));
        return channel;
    }

    private LocalDateTime resolveLastSyncAt(OtaAccountQueryRowVO row) {
        if (row.getLastRoomSyncAt() != null) {
            return row.getLastRoomSyncAt();
        }
        if (row.getAuthorizedAt() != null) {
            return row.getAuthorizedAt();
        }
        return row.getUpdatedAt();
    }

    private String latestSyncTime(List<OtaChannelVO> channels) {
        return channels.stream()
                .map(OtaChannelVO::getLastSyncAt)
                .filter(value -> value != null && !value.isBlank() && !"-".equals(value))
                .max(String::compareTo)
                .orElse("-");
    }

    private OtaChannelAuthorizationNoticeVO authorizationNotice(ChannelProfile profile, String connectionStatus) {
        OtaChannelAuthorizationNoticeVO notice = new OtaChannelAuthorizationNoticeVO();
        notice.setTitle("connected".equals(connectionStatus) ? "开始" + profile.displayName() : "关联" + profile.displayName());
        notice.setSummary("完成渠道账号或门店授权后，即可开始后续同步操作，");
        notice.setHighlight(profile.authorizationHighlight());
        notice.setSummarySuffix(" 如继续操作，代表您已阅读并同意渠道授权须知。");
        notice.setNoticeTitle(profile.displayName() + "授权须知");
        notice.setNoticeSections(List.of(noticeSection("一、授权说明", List.of("授权完成后，渠道房型、库存和价格将进入统一维护流程。"))));
        notice.setCancelLabel("取消");
        notice.setConfirmLabel("connected".equals(connectionStatus) ? "同意并开始授权" : "确认关联");
        notice.setCountdownSeconds(profile.countdownSeconds());
        notice.setBadgeText(profile.logoText());
        notice.setBadgeTone(profile.badgeTone());
        return notice;
    }

    private OtaChannelNoticeSectionVO noticeSection(String heading, List<String> paragraphs) {
        OtaChannelNoticeSectionVO section = new OtaChannelNoticeSectionVO();
        section.setHeading(heading);
        section.setParagraphs(paragraphs);
        return section;
    }

    private OtaOptionVO toStoreOption(OtaDetailStoreQueryRowVO row) {
        return option(row.getChannelStoreId(), row.getChannelStoreName());
    }

    private OtaOptionVO toAccountOption(OtaAccountQueryRowVO row) {
        return option(row.getAccountId(), row.getAccountName() == null || row.getAccountName().isBlank() ? row.getChannelName() : row.getAccountName());
    }

    private List<OtaOptionVO> buildChannelOptions(Long campId) {
        Map<String, OtaOptionVO> options = new LinkedHashMap<>();
        options.put(DEFAULT_FILTER_VALUE, option(DEFAULT_FILTER_VALUE, "全部渠道"));
        for (OtaAccountQueryRowVO row : otaMapper.selectAccounts(campId, null)) {
            ChannelProfile profile = profile(row.getChannelId(), row.getChannelName());
            options.putIfAbsent(profile.key(), option(profile.key(), profile.displayName()));
        }
        return new ArrayList<>(options.values());
    }

    private OtaLogRowVO toOtaLogRow(OtaLogQueryRowVO row) {
        ChannelProfile profile = profile(row.getChannelId(), row.getChannelName());
        OtaLogRowVO logRow = new OtaLogRowVO();
        logRow.setChannelId(profile.key());
        logRow.setChannel(profile.displayName());
        logRow.setStatus("成功");
        logRow.setOperator("系统同步");
        if (row.getRoomRelId() != null && !row.getRoomRelId().isBlank()) {
            logRow.setId("room-rel-" + row.getRoomRelId());
            logRow.setType("关联渠道房型");
            logRow.setOperationType("bindRoomType");
            logRow.setContent("关联渠道房型-" + defaultString(row.getOutRoomCategoryId(), "-")
                    + " 到 " + defaultString(row.getRoomCategoryName(), "-"));
            logRow.setTime(formatDateTime(defaultDateTime(row.getRoomRelUpdatedAt(), row.getAccountUpdatedAt())));
            return logRow;
        }
        logRow.setId("account-" + defaultString(row.getAccountId(), profile.key()));
        logRow.setType("渠道授权");
        logRow.setOperationType("bindAccount");
        logRow.setContent("渠道授权-" + profile.displayName() + "账号已完成授权");
        logRow.setTime(formatDateTime(defaultDateTime(row.getAccountAuthorizedAt(), row.getAccountUpdatedAt())));
        return logRow;
    }

    private boolean matchesLogRow(
            OtaLogRowVO row,
            String channelId,
            String operationType,
            String operationStatus,
            String keyword,
            String operator
    ) {
        if (!DEFAULT_FILTER_VALUE.equals(channelId) && !channelId.equals(row.getChannelId())) {
            return false;
        }
        if (!DEFAULT_FILTER_VALUE.equals(operationType) && !operationType.equals(row.getOperationType())) {
            return false;
        }
        if (!DEFAULT_FILTER_VALUE.equals(operationStatus)) {
            if (OPERATION_STATUS_SUCCESS.equals(operationStatus) && !"成功".equals(row.getStatus())) {
                return false;
            }
            if (OPERATION_STATUS_FAILED.equals(operationStatus) && !"失败".equals(row.getStatus())) {
                return false;
            }
        }
        if (keyword != null
                && !containsIgnoreCase(row.getContent(), keyword)
                && !containsIgnoreCase(row.getChannel(), keyword)
                && !containsIgnoreCase(row.getType(), keyword)) {
            return false;
        }
        return operator == null || containsIgnoreCase(row.getOperator(), operator);
    }

    private OtaDetailRoomRowVO toRoomRow(OtaDetailRoomQueryRowVO queryRow) {
        boolean linked = isLinked(queryRow.getShelfStatus(), queryRow.getAuditStatus());
        OtaDetailRoomRowVO row = new OtaDetailRoomRowVO();
        row.setId(queryRow.getId());
        row.setChannelStoreId(queryRow.getChannelStoreId());
        row.setChannelStoreName(queryRow.getChannelStoreName());
        row.setChannelRoomType(defaultString(queryRow.getChannelRoomType(), "-"));
        row.setStatus(linked ? "linked" : "unlinked");
        row.setStatusLabel(linked ? "已关联" : "未关联");
        row.setLinkedRoomType(linked ? defaultString(queryRow.getLinkedRoomType(), "-") : "-");
        return row;
    }

    private OtaDetailStoreRowVO toStoreRow(OtaDetailStoreQueryRowVO queryRow) {
        boolean linked = "success".equalsIgnoreCase(defaultString(queryRow.getSyncStatus(), ""));
        OtaDetailStoreRowVO row = new OtaDetailStoreRowVO();
        row.setId(queryRow.getId());
        row.setAccountId(queryRow.getAccountId());
        row.setChannelStoreId(queryRow.getChannelStoreId());
        row.setChannelStoreName(queryRow.getChannelStoreName());
        row.setHotelType("预付");
        row.setHotelId(defaultString(queryRow.getHotelId(), "-"));
        row.setRelatedRoomTypeSummary(safeInt(queryRow.getMappedRoomTypeCount()) + "/" + safeInt(queryRow.getRoomTypeCount()));
        row.setStatus(linked ? "linked" : "unlinked");
        row.setStatusLabel(linked ? "已关联" : "未关联");
        return row;
    }

    private boolean isLinked(String shelfStatus, String auditStatus) {
        return "on_shelf".equalsIgnoreCase(defaultString(shelfStatus, ""))
                && "approved".equalsIgnoreCase(defaultString(auditStatus, ""));
    }

    private OtaSyncStoreNoticeVO syncStoreNotice(String channelName) {
        OtaSyncStoreNoticeVO notice = new OtaSyncStoreNoticeVO();
        notice.setTitle("开通" + channelName);
        notice.setParagraphs(List.of(
                "1. 同步门店前请确认渠道账号授权已完成",
                "2. 建议先核对房型映射，再进行库存和价格同步"
        ));
        return notice;
    }

    private OtaSyncStoreDefaultsVO syncStoreDefaults() {
        OtaSyncStoreDefaultsVO defaults = new OtaSyncStoreDefaultsVO();
        defaults.setHotelSubtype("prepay");
        defaults.setSubHotelId("");
        defaults.setHotelName("");
        return defaults;
    }

    private OtaDashboardRequestVO requestVO(String businessDate, String storeId, String dimension) {
        OtaDashboardRequestVO request = new OtaDashboardRequestVO();
        request.setBusinessDate(businessDate);
        request.setStoreId(storeId);
        request.setDimension(dimension);
        return request;
    }

    private OtaOptionVO option(String value, String label) {
        OtaOptionVO option = new OtaOptionVO();
        option.setValue(value);
        option.setLabel(label);
        return option;
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
            throw new BusinessException(40301, "无权访问当前OTA渠道数据");
        }
        return requestedCampId;
    }

    private Long parseNullableLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalizeBusinessDate(String value) {
        if (value == null || value.isBlank()) {
            return LocalDate.now().toString();
        }
        try {
            return LocalDate.parse(value).toString();
        } catch (DateTimeParseException ex) {
            throw new BusinessException(40001, "businessDate格式错误");
        }
    }

    private String normalizeStoreId(String value) {
        return value == null || value.isBlank() ? DEFAULT_STORE : value.trim();
    }

    private String normalizeDimension(String value) {
        if (CONNECTED_DIMENSION.equals(value) || PENDING_DIMENSION.equals(value)) {
            return value;
        }
        return DEFAULT_DIMENSION;
    }

    private String normalizeChannelKey(String value) {
        return value == null || value.isBlank() ? "ctrip" : value.trim().toLowerCase(Locale.ROOT);
    }

    private Long channelKeyToId(String channelKey) {
        return CHANNEL_PROFILES.values().stream()
                .filter(profile -> profile.key().equals(channelKey))
                .map(ChannelProfile::channelId)
                .findFirst()
                .orElseGet(() -> parseNullableLong(channelKey));
    }

    private boolean isAuthorized(String status) {
        return AUTHORIZED_STATUS.equalsIgnoreCase(defaultString(status, ""));
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(DATE_TIME_FORMATTER);
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private LocalDateTime defaultDateTime(LocalDateTime primary, LocalDateTime fallback) {
        return primary != null ? primary : fallback;
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 6 : pageSize;
    }

    private String normalizeLogFilterValue(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? DEFAULT_FILTER_VALUE : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean containsIgnoreCase(String text, String keyword) {
        if (text == null || keyword == null) {
            return false;
        }
        return text.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private ChannelProfile profile(Long channelId, String fallbackName) {
        ChannelProfile predefined = CHANNEL_PROFILES.get(channelId);
        if (predefined != null) {
            return predefined;
        }
        String displayName = defaultString(fallbackName, "渠道" + channelId);
        String key = channelId == null ? displayName : String.valueOf(channelId);
        return new ChannelProfile(
                channelId,
                key,
                displayName,
                displayName.length() > 4 ? displayName.substring(0, 2) : displayName,
                5,
                "default",
                "请确保路客云房态准确后再操作直连，否则可能会导致超卖。",
                displayName + "渠道同步前请先核对门店与房型映射关系；",
                null,
                null
        );
    }

    private static Map<Long, ChannelProfile> buildChannelProfiles() {
        Map<Long, ChannelProfile> profiles = new LinkedHashMap<>();
        profiles.put(5L, new ChannelProfile(
                5L,
                "ctrip",
                "携程直连",
                "携程",
                1,
                "ctrip",
                "请确保路客云房态准确后再操作直连，否则可能会导致超卖。",
                "房型关联后佣金率将会重置为10%，如需修改，请先联系携程业务经理修改携程佣金率后再至路客云同步修改佣金率，否则将导致价格同步出错；",
                "去修改佣金率",
                null
        ));
        profiles.put(7L, new ChannelProfile(
                7L,
                "meituan-hotel",
                "美团酒店直连",
                "美团",
                4,
                "meituan",
                "请确保路客云房态准确后再操作直连，否则可能会导致超卖。",
                "如在美团酒店直连过程中出现房态无法同步的提示，请及时联系客服处理，以避免订单库存异常；",
                null,
                3
        ));
        profiles.put(8L, new ChannelProfile(8L, "fliggy", "飞猪酒店", "飞猪", 3, "default", "请确认飞猪账号授权已完成。", "飞猪渠道调价后请及时核对售卖库存与活动价，避免渠道侧活动冲突影响同步结果；", null, null));
        profiles.put(9L, new ChannelProfile(9L, "meituan-homestay", "美团民宿", "美宿", 4, "meituan", "请确认美团民宿账号授权已完成。", "美团民宿关联房型后，请留意民宿侧退款规则和早餐规则是否同步一致；", null, null));
        profiles.put(10L, new ChannelProfile(10L, "tujia", "途家", "途家", 2, "default", "请确认途家账号授权已完成。", "途家渠道同步前请优先确认佣金率、早餐和退改规则，避免价格计算差异；", null, null));
        profiles.put(21L, new ChannelProfile(21L, "muniao", "木鸟", "木鸟", 5, "default", "请确认木鸟账号授权已完成。", "木鸟渠道同步后，请检查房态和最短入住限制，避免前台售卖规则不一致；", null, null));
        profiles.put(20L, new ChannelProfile(20L, "xiaozhu", "小猪", "小猪", 3, "default", "请确认小猪账号授权已完成。", "小猪渠道同步时请确认民宿规则、入住须知和价格计划保持一致；", null, null));
        profiles.put(17L, new ChannelProfile(17L, "locals", "路客云聚合", "路客", 1, "default", "请确认聚合渠道账号授权已完成。", "路客云聚合渠道同步后，请优先核对聚合分发房型与本地房型映射是否完整；", null, null));
        profiles.put(22L, new ChannelProfile(22L, "booking", "Booking", "Bo", 5, "default", "请确认Booking账号授权已完成。", "Booking渠道同步前请核对门店与房型映射关系；", null, null));
        return Map.copyOf(profiles);
    }

    private record ChannelProfile(
            Long channelId,
            String key,
            String displayName,
            String logoText,
            Integer logoTone,
            String badgeTone,
            String authorizationHighlight,
            String noticeText,
            String noticeLinkLabel,
            Integer countdownSeconds
    ) {
    }
}
