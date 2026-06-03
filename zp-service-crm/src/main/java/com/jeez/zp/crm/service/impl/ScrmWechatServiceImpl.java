package com.jeez.zp.crm.service.impl;

import com.jeez.zp.crm.dto.request.ScrmWechatQueryRequest;
import com.jeez.zp.crm.mapper.ScrmWechatMapper;
import com.jeez.zp.crm.service.CampAccessService;
import com.jeez.zp.crm.service.ScrmWechatService;
import com.jeez.zp.crm.vo.ScrmLookupOptionVO;
import com.jeez.zp.crm.vo.ScrmMetricVO;
import com.jeez.zp.crm.vo.ScrmPendingItemVO;
import com.jeez.zp.crm.vo.ScrmReplyTemplateVO;
import com.jeez.zp.crm.vo.ScrmRoomSuggestionRowVO;
import com.jeez.zp.crm.vo.ScrmRoomSuggestionVO;
import com.jeez.zp.crm.vo.ScrmSidebarConversationVO;
import com.jeez.zp.crm.vo.ScrmSidebarDashboardResponseVO;
import com.jeez.zp.crm.vo.ScrmSidebarExportResponseVO;
import com.jeez.zp.crm.vo.ScrmTrendPointVO;
import com.jeez.zp.crm.vo.ScrmWechatConversationRowVO;
import com.jeez.zp.crm.vo.WechatKfAccountPageResponseVO;
import com.jeez.zp.crm.vo.WechatKfAccountReportResponseVO;
import com.jeez.zp.crm.vo.WechatKfAccountVO;
import com.jeez.zp.crm.vo.WechatKfConversationVO;
import com.jeez.zp.crm.vo.WechatKfSummaryVO;
import com.jeez.zp.crm.vo.WechatKfTodoVO;
import com.jeez.zp.crm.vo.WechatPaginationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScrmWechatServiceImpl implements ScrmWechatService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MM.dd");
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("HH:00");

    private final ScrmWechatMapper scrmWechatMapper;
    private final CampAccessService campAccessService;

    @Override
    public WechatKfAccountPageResponseVO getKfAccounts(ScrmWechatQueryRequest request, Long userId) {
        ScrmWechatQueryRequest safeRequest = safeRequest(request);
        Long campId = campAccessService.resolveAccessibleCampId(parseLong(safeRequest.getCampId()), userId);
        int pageNum = normalizePageNum(safeRequest);
        int pageSize = normalizePageSize(safeRequest.getPageSize());

        List<WechatKfAccountVO> rows = scrmWechatMapper.selectKfAccounts(campId);
        PageSlice<WechatKfAccountVO> pageSlice = pageSlice(rows, pageNum, pageSize);

        WechatKfAccountPageResponseVO response = new WechatKfAccountPageResponseVO();
        response.setTotal(pageSlice.total());
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setList(pageSlice.items());
        return response;
    }

    @Override
    public WechatKfAccountReportResponseVO getKfReport(ScrmWechatQueryRequest request, Long userId) {
        ScrmWechatQueryRequest safeRequest = safeRequest(request);
        Long campId = campAccessService.resolveAccessibleCampId(parseLong(safeRequest.getCampId()), userId);
        int pageNum = normalizePageNum(safeRequest);
        int pageSize = normalizePageSize(safeRequest.getPageSize());

        List<WechatKfConversationVO> rows = loadFilteredRows(campId, null, safeRequest).stream()
                .map(this::toWechatConversation)
                .toList();
        PageSlice<WechatKfConversationVO> pageSlice = pageSlice(rows, pageNum, pageSize);

        WechatKfAccountReportResponseVO response = new WechatKfAccountReportResponseVO();
        response.setSummary(buildWechatSummary(rows));
        response.setConversations(pageSlice.items());
        response.setTodos(buildWechatTodos(rows));
        response.setTotal(pageSlice.total());
        response.setPagination(new WechatPaginationVO(pageNum, pageSize, pageSlice.total()));
        return response;
    }

    @Override
    public ScrmSidebarDashboardResponseVO getSidebarDashboard(ScrmWechatQueryRequest request, Long userId) {
        ScrmWechatQueryRequest safeRequest = safeRequest(request);
        Long campId = campAccessService.resolveAccessibleCampId(parseLong(safeRequest.getCampId()), userId);
        Long poiId = parseNullableFilterLong(safeRequest.getPoiId());
        int pageNum = normalizePageNum(safeRequest);
        int pageSize = normalizePageSize(safeRequest.getPageSize());

        List<ScrmSidebarConversationVO> rows = loadFilteredRows(campId, poiId, safeRequest).stream()
                .map(this::toSidebarConversation)
                .toList();
        PageSlice<ScrmSidebarConversationVO> pageSlice = pageSlice(rows, pageNum, pageSize);

        ScrmSidebarDashboardResponseVO response = new ScrmSidebarDashboardResponseVO();
        response.setStores(scrmWechatMapper.selectStores(campId));
        response.setChannels(buildChannels(campId));
        response.setMetrics(buildSidebarMetrics(rows));
        response.setConversations(pageSlice.items());
        response.setPendingItems(buildPendingItems(rows));
        response.setReplyTemplates(scrmWechatMapper.selectReplyTemplates(campId));
        response.setRoomSuggestions(buildRoomSuggestions(campId, poiId));
        response.setTrend(buildTrend(rows));
        response.setPagination(new WechatPaginationVO(pageNum, pageSize, pageSlice.total()));
        return response;
    }

    @Override
    public ScrmSidebarExportResponseVO exportSidebar(ScrmWechatQueryRequest request, Long userId) {
        ScrmWechatQueryRequest safeRequest = safeRequest(request);
        Long campId = campAccessService.resolveAccessibleCampId(parseLong(safeRequest.getCampId()), userId);
        Long poiId = parseNullableFilterLong(safeRequest.getPoiId());
        List<ScrmSidebarConversationVO> rows = loadFilteredRows(campId, poiId, safeRequest).stream()
                .map(this::toSidebarConversation)
                .toList();
        String dateToken = resolveExportDate(safeRequest).replace("-", "");

        ScrmSidebarExportResponseVO response = new ScrmSidebarExportResponseVO();
        response.setTaskId("SCRM-SIDEBAR-EXPORT-" + campId + "-" + dateToken);
        response.setFileName("scrm_sidebar_" + dateToken + ".csv");
        response.setContentType("text/csv");
        response.setTotal(rows.size());
        response.setRows(rows);
        return response;
    }

    private List<ScrmWechatConversationRowVO> loadFilteredRows(Long campId, Long poiId, ScrmWechatQueryRequest request) {
        DateRange range = resolveDateRange(request);
        return scrmWechatMapper.selectConversationRows(campId, poiId, range.start(), range.endExclusive()).stream()
                .filter(row -> matchesChannel(row, request.getChannel()))
                .filter(row -> matchesStatus(row, request.getStatus()))
                .filter(row -> matchesKeyword(row, request.getKeyword()))
                .toList();
    }

    private WechatKfConversationVO toWechatConversation(ScrmWechatConversationRowVO row) {
        String status = toWechatStatus(row.getOrderStatus());
        WechatKfConversationVO conversation = new WechatKfConversationVO();
        conversation.setId("WX-" + row.getOrderId());
        conversation.setCustomerName(nonBlank(row.getGuestName(), "-"));
        conversation.setChannel(toChannelCode(row.getChannelName()));
        conversation.setChannelName(nonBlank(row.getChannelName(), "未知渠道"));
        conversation.setStatus(status);
        conversation.setStatusName(toWechatStatusName(status));
        conversation.setOrderStatus(toWechatStatusName(status));
        conversation.setStayDate(formatDate(row.getStartAt()) + " 至 " + formatDate(row.getEndAt()));
        conversation.setRoomType(nonBlank(row.getRoomCategoryName(), "-"));
        conversation.setLastMessage(nonBlank(row.getRemark(), "订单沟通待跟进"));
        conversation.setLastMessageAt(formatDateTime(row.getCreatedAt()));
        conversation.setAssignee(nonBlank(row.getAssignee(), "未分配"));
        conversation.setUnread("pendingCheckIn".equals(status) ? 1 : 0);
        return conversation;
    }

    private ScrmSidebarConversationVO toSidebarConversation(ScrmWechatConversationRowVO row) {
        String status = toWechatStatus(row.getOrderStatus());
        ScrmSidebarConversationVO conversation = new ScrmSidebarConversationVO();
        conversation.setId("conv-" + row.getOrderId());
        conversation.setGuestName(nonBlank(row.getGuestName(), "-"));
        conversation.setChannel(toChannelCode(row.getChannelName()));
        conversation.setRoomName(joinRoomLabel(row.getRoomCategoryName(), row.getRoomName()));
        conversation.setStatus(toSidebarStatusName(status));
        conversation.setLastMessage(nonBlank(row.getRemark(), "订单沟通待跟进"));
        conversation.setLastSender(nonBlank(row.getAssignee(), "未分配"));
        conversation.setLastMessageAt(formatDateTime(row.getCreatedAt()));
        conversation.setResponseSla("96秒内");
        conversation.setOrderNo(nonBlank(row.getOrderNo(), "-"));
        conversation.setStayRange(formatStayRange(row.getStartAt(), row.getEndAt()));
        conversation.setTags(buildConversationTags(status));
        conversation.setPreference(nonBlank(row.getRemark(), "关注入住指引与房态联动"));
        conversation.setOrderAmount(formatCent(row.getTotalPayPriceCent()));
        return conversation;
    }

    private WechatKfSummaryVO buildWechatSummary(List<WechatKfConversationVO> rows) {
        int pending = (int) rows.stream().filter(row -> row.getUnread() != null && row.getUnread() > 0).count();
        int converted = (int) rows.stream().filter(row -> "checkedIn".equals(row.getStatus())).count();
        WechatKfSummaryVO summary = new WechatKfSummaryVO();
        summary.setTodaySessions(rows.size());
        summary.setPendingSessions(pending);
        summary.setAverageReplySeconds(rows.isEmpty() ? 0 : 96);
        summary.setConversionLeads(converted);
        summary.setResponseRate(rows.isEmpty() ? "0%" : "100%");
        return summary;
    }

    private List<WechatKfTodoVO> buildWechatTodos(List<WechatKfConversationVO> rows) {
        int unread = (int) rows.stream().filter(row -> row.getUnread() != null && row.getUnread() > 0).count();
        int pendingCheckIn = (int) rows.stream().filter(row -> "pendingCheckIn".equals(row.getStatus())).count();
        return List.of(
                new WechatKfTodoVO("todo-unread", "待回复会话", unread, "优先处理最近未读咨询"),
                new WechatKfTodoVO("todo-checkin", "待入住咨询", pendingCheckIn, "同步入住指引与门锁信息"),
                new WechatKfTodoVO("todo-transfer", "需转接客服", 0, "分配给在线接待人员")
        );
    }

    private List<ScrmLookupOptionVO> buildChannels(Long campId) {
        List<ScrmLookupOptionVO> channels = new ArrayList<>();
        channels.add(new ScrmLookupOptionVO("ALL", "全部渠道"));
        channels.addAll(scrmWechatMapper.selectChannels(campId));
        return channels;
    }

    private List<ScrmMetricVO> buildSidebarMetrics(List<ScrmSidebarConversationVO> rows) {
        long waiting = rows.stream().filter(row -> "待回复".equals(row.getStatus())).count();
        long converted = rows.stream().filter(row -> "已转订单".equals(row.getStatus()) || "入住中".equals(row.getStatus())).count();
        return List.of(
                new ScrmMetricVO("sessions", "今日会话", String.valueOf(rows.size()), "+0%", "blue", "按当前筛选条件统计"),
                new ScrmMetricVO("waiting", "待回复", String.valueOf(waiting), waiting > 0 ? "需处理" : "已清空", waiting > 0 ? "red" : "green", "按响应 SLA 排序"),
                new ScrmMetricVO("orders", "转化订单", String.valueOf(converted), "订单联动", "green", "可跳转住宿订单核对"),
                new ScrmMetricVO("response", "平均响应", rows.isEmpty() ? "0秒" : "96秒", "达标", "orange", "统计在线客服首响")
        );
    }

    private List<ScrmPendingItemVO> buildPendingItems(List<ScrmSidebarConversationVO> rows) {
        return rows.stream()
                .filter(row -> "待回复".equals(row.getStatus()))
                .limit(5)
                .map(row -> new ScrmPendingItemVO("pending-" + row.getId(), "跟进" + row.getGuestName(), row.getLastSender(), "尽快", "high"))
                .toList();
    }

    private List<ScrmRoomSuggestionVO> buildRoomSuggestions(Long campId, Long poiId) {
        return scrmWechatMapper.selectRoomSuggestions(campId, poiId).stream()
                .map(this::toRoomSuggestion)
                .toList();
    }

    private ScrmRoomSuggestionVO toRoomSuggestion(ScrmRoomSuggestionRowVO row) {
        ScrmRoomSuggestionVO suggestion = new ScrmRoomSuggestionVO();
        suggestion.setId(row.getId());
        suggestion.setRoomName(row.getRoomName());
        suggestion.setStatus(row.getStatus());
        suggestion.setAvailableTonight(row.getAvailableTonight());
        suggestion.setAction(row.getAction());
        return suggestion;
    }

    private List<ScrmTrendPointVO> buildTrend(List<ScrmSidebarConversationVO> rows) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (ScrmSidebarConversationVO row : rows) {
            String label = row.getLastMessageAt() == null || row.getLastMessageAt().length() < 13
                    ? "--:00"
                    : row.getLastMessageAt().substring(11, 13) + ":00";
            counts.merge(label, 1, Integer::sum);
        }
        return counts.entrySet().stream()
                .map(entry -> new ScrmTrendPointVO(entry.getKey(), entry.getValue(), entry.getValue()))
                .toList();
    }

    private List<String> buildConversationTags(String status) {
        if ("pendingCheckIn".equals(status)) {
            return List.of("待入住", "企微客服");
        }
        if ("checkedIn".equals(status)) {
            return List.of("入住中", "已转订单");
        }
        if ("cancelled".equals(status)) {
            return List.of("已取消");
        }
        return List.of("咨询中");
    }

    private boolean matchesChannel(ScrmWechatConversationRowVO row, String channel) {
        String normalized = trimToNull(channel);
        if (normalized == null || "ALL".equalsIgnoreCase(normalized)) {
            return true;
        }
        return normalized.equalsIgnoreCase(toChannelCode(row.getChannelName()));
    }

    private boolean matchesStatus(ScrmWechatConversationRowVO row, String status) {
        String normalized = trimToNull(status);
        if (normalized == null || "ALL".equalsIgnoreCase(normalized)) {
            return true;
        }
        return normalized.equalsIgnoreCase(toWechatStatus(row.getOrderStatus()));
    }

    private boolean matchesKeyword(ScrmWechatConversationRowVO row, String keyword) {
        String normalized = trimToNull(keyword);
        if (normalized == null) {
            return true;
        }
        String lowerKeyword = normalized.toLowerCase(Locale.ROOT);
        return contains(row.getOrderNo(), lowerKeyword)
                || contains(row.getGuestName(), lowerKeyword)
                || contains(row.getGuestMobile(), lowerKeyword)
                || contains(row.getRoomCategoryName(), lowerKeyword)
                || contains(row.getRoomName(), lowerKeyword)
                || contains(row.getRemark(), lowerKeyword)
                || contains(row.getAssignee(), lowerKeyword);
    }

    private boolean contains(String value, String lowerKeyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(lowerKeyword);
    }

    private String toChannelCode(String channelName) {
        String value = nonBlank(channelName, "").toLowerCase(Locale.ROOT);
        if (value.contains("美团") || value.contains("meituan")) {
            return "meituan";
        }
        if (value.contains("携程") || value.contains("ctrip")) {
            return "ctrip";
        }
        if (value.contains("小猪") || value.contains("xiaozhu")) {
            return "xiaozhu";
        }
        if (value.contains("途家") || value.contains("去哪") || value.contains("tujia")) {
            return "tujia";
        }
        return "other";
    }

    private String toWechatStatus(String orderStatus) {
        String status = nonBlank(orderStatus, "").toLowerCase(Locale.ROOT);
        if (status.contains("pending") || status.contains("waiting")) {
            return "pendingCheckIn";
        }
        if (status.contains("cancel")) {
            return "cancelled";
        }
        if (status.contains("checked_in") || status.contains("living") || status.contains("checkin")) {
            return "checkedIn";
        }
        if (status.contains("finished") || status.contains("completed") || status.contains("checked_out")) {
            return "checkedIn";
        }
        return "consulting";
    }

    private String toWechatStatusName(String status) {
        return switch (status) {
            case "pendingCheckIn" -> "待入住";
            case "checkedIn" -> "入住中";
            case "cancelled" -> "已取消";
            default -> "咨询中";
        };
    }

    private String toSidebarStatusName(String status) {
        return switch (status) {
            case "pendingCheckIn" -> "待回复";
            case "checkedIn" -> "已转订单";
            case "cancelled" -> "已取消";
            default -> "咨询中";
        };
    }

    private DateRange resolveDateRange(ScrmWechatQueryRequest request) {
        String startDate = trimToNull(request.getStartDate());
        String endDate = trimToNull(request.getEndDate());
        if (startDate == null && endDate == null) {
            String statDate = trimToNull(request.getStatDate());
            startDate = statDate;
            endDate = statDate;
        }
        LocalDateTime start = startDate == null ? null : LocalDate.parse(startDate, DATE_FORMATTER).atStartOfDay();
        LocalDateTime endExclusive = endDate == null ? null : LocalDate.parse(endDate, DATE_FORMATTER).plusDays(1).atStartOfDay();
        return new DateRange(start, endExclusive);
    }

    private String resolveExportDate(ScrmWechatQueryRequest request) {
        String statDate = trimToNull(request.getStatDate());
        if (statDate != null) {
            return statDate;
        }
        String startDate = trimToNull(request.getStartDate());
        if (startDate != null) {
            return startDate;
        }
        return LocalDate.now().format(DATE_FORMATTER);
    }

    private ScrmWechatQueryRequest safeRequest(ScrmWechatQueryRequest request) {
        return request == null ? new ScrmWechatQueryRequest() : request;
    }

    private int normalizePageNum(ScrmWechatQueryRequest request) {
        Integer candidate = request.getPageNum() != null ? request.getPageNum() : (request.getPage() != null ? request.getPage() : request.getCurrent());
        return candidate == null || candidate < 1 ? DEFAULT_PAGE_NUM : candidate;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private Long parseNullableFilterLong(String value) {
        String normalized = trimToNull(value);
        if (normalized == null || "ALL".equalsIgnoreCase(normalized)) {
            return null;
        }
        return Long.valueOf(normalized);
    }

    private Long parseLong(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : Long.valueOf(normalized);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String joinRoomLabel(String roomCategoryName, String roomName) {
        String category = nonBlank(roomCategoryName, "-");
        String room = trimToNull(roomName);
        return room == null ? category : category + " / " + room;
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "-" : value.toLocalDate().format(DATE_FORMATTER);
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(DATE_TIME_FORMATTER);
    }

    private String formatStayRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            return "-";
        }
        long nights = Math.max(1, java.time.Duration.between(startAt.toLocalDate().atStartOfDay(), endAt.toLocalDate().atStartOfDay()).toDays());
        return startAt.format(MONTH_DAY_FORMATTER) + "-" + endAt.format(MONTH_DAY_FORMATTER) + "（" + nights + "晚）";
    }

    private String formatCent(Long cent) {
        BigDecimal amount = BigDecimal.valueOf(cent == null ? 0L : cent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return amount.toPlainString();
    }

    private <T> PageSlice<T> pageSlice(List<T> rows, int pageNum, int pageSize) {
        long total = rows.size();
        int fromIndex = Math.min((pageNum - 1) * pageSize, rows.size());
        int toIndex = Math.min(fromIndex + pageSize, rows.size());
        return new PageSlice<>(total, rows.subList(fromIndex, toIndex));
    }

    private record DateRange(LocalDateTime start, LocalDateTime endExclusive) {
    }

    private record PageSlice<T>(long total, List<T> items) {
    }
}
