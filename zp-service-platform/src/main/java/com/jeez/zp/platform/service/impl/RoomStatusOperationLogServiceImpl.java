package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.RoomStatusOperationLogMapper;
import com.jeez.zp.platform.service.RoomStatusOperationLogService;
import com.jeez.zp.platform.vo.ChannelRoomStatusOperationLogViewVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.RoomStatusOperationLogPageResponseVO;
import com.jeez.zp.platform.vo.RoomStatusOperationLogQueryRowVO;
import com.jeez.zp.platform.vo.RoomStatusOperationLogRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RoomStatusOperationLogServiceImpl implements RoomStatusOperationLogService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String DIRECT_CHANNEL_ID = "0";
    private static final String DIRECT_CHANNEL_NAME = "自来客";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RoomStatusOperationLogMapper roomStatusOperationLogMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public RoomStatusOperationLogPageResponseVO getPage(
            Long campId,
            Long userId,
            Integer pageNum,
            Integer pageSize,
            Integer current,
            String keyword,
            Integer adjustType,
            String channelId,
            String startDate,
            String endDate,
            String createStartTime,
            String createEndTime,
            String userName
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = normalizePageNum(pageNum, current);
        int resolvedPageSize = normalizePageSize(pageSize);
        FilterSpec filterSpec = new FilterSpec(
                trimToNull(keyword),
                adjustType,
                normalizeChannelId(channelId),
                parseDate(startDate),
                parseDate(endDate),
                parseDateTimeStart(createStartTime),
                parseDateTimeEnd(createEndTime),
                trimToNull(userName)
        );

        List<RoomStatusOperationLogRecordVO> matched = roomStatusOperationLogMapper.selectRows(resolvedCampId).stream()
                .filter(row -> matches(row, filterSpec))
                .map(this::toRecord)
                .toList();

        long total = matched.size();
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / resolvedPageSize);
        int fromIndex = Math.min((resolvedPageNum - 1) * resolvedPageSize, matched.size());
        int toIndex = Math.min(fromIndex + resolvedPageSize, matched.size());

        RoomStatusOperationLogPageResponseVO response = new RoomStatusOperationLogPageResponseVO();
        response.setTotal(total);
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setPageNum(resolvedPageNum);
        response.setHasNextPage(resolvedPageNum < pages);
        response.setPages(pages);
        response.setList(matched.subList(fromIndex, toIndex));
        return response;
    }

    private boolean matches(RoomStatusOperationLogQueryRowVO row, FilterSpec filterSpec) {
        DerivedLog derivedLog = deriveLog(row);

        if (filterSpec.adjustType() != null && !filterSpec.adjustType().equals(derivedLog.adjustType())) {
            return false;
        }
        if (filterSpec.channelId() != null && !filterSpec.channelId().equals(derivedLog.channelId())) {
            return false;
        }
        if (filterSpec.startDate() != null && derivedLog.startDate().isBefore(filterSpec.startDate())) {
            return false;
        }
        if (filterSpec.endDate() != null && derivedLog.endDate().isAfter(filterSpec.endDate())) {
            return false;
        }
        if (filterSpec.createStartAt() != null && row.getCreateTime().isBefore(filterSpec.createStartAt())) {
            return false;
        }
        if (filterSpec.createEndAt() != null && row.getCreateTime().isAfter(filterSpec.createEndAt())) {
            return false;
        }
        if (filterSpec.userName() != null && !containsIgnoreCase(row.getUserName(), filterSpec.userName())) {
            return false;
        }
        if (filterSpec.keyword() == null) {
            return true;
        }

        return containsIgnoreCase(row.getRoomCategoryName(), filterSpec.keyword())
                || containsIgnoreCase(row.getRoomName(), filterSpec.keyword())
                || containsIgnoreCase(derivedLog.operationContent(), filterSpec.keyword())
                || containsIgnoreCase(derivedLog.adjustContent(), filterSpec.keyword())
                || containsIgnoreCase(row.getUserName(), filterSpec.keyword())
                || containsIgnoreCase(derivedLog.channelName(), filterSpec.keyword())
                || containsIgnoreCase(row.getChannelRoomCategoryProductName(), filterSpec.keyword());
    }

    private RoomStatusOperationLogRecordVO toRecord(RoomStatusOperationLogQueryRowVO row) {
        DerivedLog derivedLog = deriveLog(row);
        ChannelRoomStatusOperationLogViewVO channelView = new ChannelRoomStatusOperationLogViewVO();
        channelView.setChannelName(derivedLog.channelName());
        channelView.setChannelRoomCategoryProductName(defaultString(row.getChannelRoomCategoryProductName(), row.getRoomCategoryName()));
        channelView.setStockContent(derivedLog.stockContent());
        channelView.setIsSuccess(1);
        channelView.setErrorMsg(null);

        RoomStatusOperationLogRecordVO record = new RoomStatusOperationLogRecordVO();
        record.setRoomStatusOperationLogId(row.getRoomStatusOperationLogId());
        record.setRoomCategoryName(row.getRoomCategoryName());
        record.setRoomName(row.getRoomName());
        record.setStartDate(derivedLog.startDate().format(DATE_FORMATTER));
        record.setEndDate(derivedLog.endDate().format(DATE_FORMATTER));
        record.setOperationContent(derivedLog.operationContent());
        record.setAdjustContent(derivedLog.adjustContent());
        record.setUserName(row.getUserName());
        record.setCreateTime(row.getCreateTime().format(DATE_TIME_FORMATTER));
        record.setChannelRoomStatusOperationLogViews(List.of(channelView));
        return record;
    }

    private DerivedLog deriveLog(RoomStatusOperationLogQueryRowVO row) {
        boolean manual = row.getChannelId() == null || row.getChannelId().isBlank()
                || "frontdesk".equalsIgnoreCase(row.getSourceType())
                || "phone".equalsIgnoreCase(row.getSourceType());
        String channelId = manual ? DIRECT_CHANNEL_ID : row.getChannelId();
        String channelName = manual ? DIRECT_CHANNEL_NAME : defaultString(row.getChannelName(), DIRECT_CHANNEL_NAME);
        String operationContent = manual ? "同步房态" : "渠道库存变更";
        String adjustContent = manual ? "手动调整" : "系统调整";
        LocalDate startDate = row.getStartAt().toLocalDate();
        LocalDate endDate = row.getEndAt().toLocalDate().minusDays(1);
        if (endDate.isBefore(startDate)) {
            endDate = startDate;
        }
        return new DerivedLog(
                manual ? 1 : 2,
                channelId,
                channelName,
                operationContent,
                adjustContent,
                "关",
                startDate,
                endDate
        );
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
            throw new BusinessException(40301, "无权访问当前门店房态日志");
        }
        return requestedCampId;
    }

    private int normalizePageNum(Integer pageNum, Integer current) {
        Integer candidate = pageNum != null ? pageNum : current;
        return candidate == null || candidate < 1 ? DEFAULT_PAGE_NUM : candidate;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private String normalizeChannelId(String channelId) {
        String trimmed = trimToNull(channelId);
        if (trimmed == null) {
            return null;
        }
        return trimmed;
    }

    private LocalDate parseDate(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        return LocalDate.parse(trimmed.substring(0, 10));
    }

    private LocalDateTime parseDateTimeStart(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        if (trimmed.length() == 10) {
            return LocalDate.parse(trimmed).atStartOfDay();
        }
        return LocalDateTime.parse(trimmed.replace(" ", "T"));
    }

    private LocalDateTime parseDateTimeEnd(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        if (trimmed.length() == 10) {
            return LocalDate.parse(trimmed).atTime(LocalTime.MAX);
        }
        return LocalDateTime.parse(trimmed.replace(" ", "T"));
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

    private String defaultString(String first, String fallback) {
        return first == null || first.isBlank() ? fallback : first;
    }

    private record FilterSpec(
            String keyword,
            Integer adjustType,
            String channelId,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime createStartAt,
            LocalDateTime createEndAt,
            String userName
    ) {
    }

    private record DerivedLog(
            Integer adjustType,
            String channelId,
            String channelName,
            String operationContent,
            String adjustContent,
            String stockContent,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }
}
