package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.EditionReplaceOrderMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.EditionReplaceOrderService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.EditionReplaceOrderPageResponseVO;
import com.jeez.zp.platform.vo.EditionReplaceOrderQueryRowVO;
import com.jeez.zp.platform.vo.EditionReplaceOrderRowVO;
import com.jeez.zp.platform.vo.EditionReplaceOrderSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EditionReplaceOrderServiceImpl implements EditionReplaceOrderService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final EditionReplaceOrderMapper editionReplaceOrderMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public EditionReplaceOrderPageResponseVO getReplaceOrders(
            Long campId,
            Long userId,
            Long receiverStartTime,
            Long receiverEndTime,
            Integer pageNum,
            Integer current,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = normalizePageNum(pageNum, current);
        int resolvedPageSize = normalizePageSize(pageSize);

        List<EditionReplaceOrderRowVO> rows = editionReplaceOrderMapper.selectReplaceOrderRows(
                        resolvedCampId,
                        toLocalDateTime(receiverStartTime),
                        toLocalDateTime(receiverEndTime)
                ).stream()
                .map(this::toRow)
                .toList();
        PageSlice<EditionReplaceOrderRowVO> pageSlice = pageSlice(rows, resolvedPageNum, resolvedPageSize);

        EditionReplaceOrderPageResponseVO response = new EditionReplaceOrderPageResponseVO();
        response.setTotal(pageSlice.total());
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setPageNum(resolvedPageNum);
        response.setPageSize(resolvedPageSize);
        response.setPages(resolvePages(pageSlice.total(), resolvedPageSize));
        response.setHasNextPage((long) resolvedPageNum * resolvedPageSize < pageSlice.total());
        response.setSummary(buildSummary(rows));
        response.setList(pageSlice.items());
        return response;
    }

    private EditionReplaceOrderRowVO toRow(EditionReplaceOrderQueryRowVO source) {
        String settlementStatus = resolveSettlementStatus(source.getOrderStatus());

        EditionReplaceOrderRowVO row = new EditionReplaceOrderRowVO();
        row.setReplaceOrderId("replace-" + source.getOrderId());
        row.setOrderId(String.valueOf(source.getOrderId()));
        row.setOrderNo(nonBlank(source.getOrderNo(), "ORDER-" + source.getOrderId()));
        row.setChannelOrderNo(nonBlank(source.getOutOrderNo(), "-"));
        row.setReplaceMonth(formatMonth(source.getCreatedAt()));
        row.setChannelName(nonBlank(source.getChannelName(), "未知渠道"));
        row.setRoomCategoryName(nonBlank(source.getRoomCategoryName(), "未关联房型"));
        row.setRoomName(nonBlank(source.getRoomName(), "-"));
        row.setContactName(nonBlank(source.getGuestName(), "-"));
        row.setContactMobile(nonBlank(source.getGuestMobile(), "-"));
        row.setStayStatus(resolveStayStatus(source.getOrderStatus()));
        row.setStayStatusName(resolveStayStatusName(row.getStayStatus()));
        row.setSettlementStatus(settlementStatus);
        row.setSettlementStatusName(resolveSettlementStatusName(settlementStatus));
        row.setCheckInDate(formatDate(source.getStartAt()));
        row.setCheckOutDate(formatDate(source.getEndAt()));
        row.setSettlementDate(formatDate(source.getCreatedAt()));
        row.setSettlementAmount(defaultLong(source.getSettlementAmountCent()));
        row.setReplaceAmount(resolveReplaceAmount(source));
        row.setRemark(nonBlank(source.getRemark(), "-"));
        return row;
    }

    private EditionReplaceOrderSummaryVO buildSummary(List<EditionReplaceOrderRowVO> rows) {
        EditionReplaceOrderSummaryVO summary = new EditionReplaceOrderSummaryVO();
        summary.setPendingReplaceAmount(rows.stream()
                .filter(row -> "pending".equals(row.getSettlementStatus()))
                .mapToLong(row -> defaultLong(row.getReplaceAmount()))
                .sum());
        summary.setCompletedReplaceAmount(rows.stream()
                .filter(row -> "completed".equals(row.getSettlementStatus()))
                .mapToLong(row -> defaultLong(row.getReplaceAmount()))
                .sum());
        return summary;
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "当前用户上下文不存在");
        }
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!Objects.equals(requestedCampId, bundle.getCampId())) {
            throw new BusinessException(40301, "无权访问当前门店置换权益数据");
        }
        return requestedCampId;
    }

    private LocalDateTime toLocalDateTime(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        return Instant.ofEpochMilli(epochMillis).atZone(SHANGHAI_ZONE).toLocalDateTime();
    }

    private int normalizePageNum(Integer pageNum, Integer current) {
        Integer candidate = pageNum == null ? current : pageNum;
        return candidate == null || candidate < 1 ? DEFAULT_PAGE_NUM : candidate;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private int resolvePages(long total, int pageSize) {
        if (total <= 0) {
            return 0;
        }
        return (int) Math.ceil(total * 1.0 / pageSize);
    }

    private <T> PageSlice<T> pageSlice(List<T> rows, int pageNum, int pageSize) {
        long total = rows.size();
        int fromIndex = Math.min((pageNum - 1) * pageSize, rows.size());
        int toIndex = Math.min(fromIndex + pageSize, rows.size());
        return new PageSlice<>(total, rows.subList(fromIndex, toIndex));
    }

    private String resolveStayStatus(String orderStatus) {
        if ("checked_out".equals(orderStatus) || "completed".equals(orderStatus) || "finished".equals(orderStatus)) {
            return "checkedOut";
        }
        if ("pending".equals(orderStatus) || "waiting".equals(orderStatus)) {
            return "waiting";
        }
        return "living";
    }

    private String resolveStayStatusName(String stayStatus) {
        return switch (stayStatus) {
            case "waiting" -> "待入住";
            case "checkedOut" -> "已退房";
            default -> "入住中";
        };
    }

    private String resolveSettlementStatus(String orderStatus) {
        if ("checked_out".equals(orderStatus) || "completed".equals(orderStatus) || "finished".equals(orderStatus)) {
            return "completed";
        }
        return "pending";
    }

    private String resolveSettlementStatusName(String settlementStatus) {
        return "completed".equals(settlementStatus) ? "已置换" : "待置换";
    }

    private String formatDate(LocalDateTime value) {
        if (value == null) {
            return "";
        }
        return value.toLocalDate().format(DATE_FORMATTER);
    }

    private String formatMonth(LocalDateTime value) {
        LocalDate date = value == null ? LocalDate.now(SHANGHAI_ZONE) : value.toLocalDate();
        return date.format(MONTH_FORMATTER);
    }

    private Long resolveReplaceAmount(EditionReplaceOrderQueryRowVO row) {
        Long replaceAmount = row.getReplaceAmountCent();
        if (replaceAmount != null && replaceAmount > 0L) {
            return replaceAmount;
        }
        return defaultLong(row.getSettlementAmountCent());
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private record PageSlice<T>(long total, List<T> items) {
    }
}
