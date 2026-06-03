package com.jeez.zp.room.service.impl;

import com.jeez.zp.room.dto.request.PriceLogPageRequest;
import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.PriceLogMapper;
import com.jeez.zp.room.mapper.UserCampMapper;
import com.jeez.zp.room.service.PriceLogService;
import com.jeez.zp.room.vo.PriceLogDictionariesVO;
import com.jeez.zp.room.vo.PriceLogExportResponseVO;
import com.jeez.zp.room.vo.PriceLogOptionVO;
import com.jeez.zp.room.vo.PriceLogPageResponseVO;
import com.jeez.zp.room.vo.PriceLogPaginationVO;
import com.jeez.zp.room.vo.PriceLogRowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PriceLogServiceImpl implements PriceLogService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String SELF_CHANNEL_NAME = "\u81ea\u6765\u5ba2";
    private static final String MANUAL_ADJUST_TYPE = "manual";
    private static final String SYSTEM_ADJUST_TYPE = "system";
    private static final String MANUAL_ADJUST_TYPE_NAME = "\u624b\u52a8\u8c03\u6574";
    private static final String SYSTEM_ADJUST_TYPE_NAME = "\u7cfb\u7edf\u8c03\u6574";
    private static final String SYSTEM_OPERATOR_NAME = "\u7cfb\u7edf\u540c\u6b65";
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final PriceLogMapper priceLogMapper;
    private final UserCampMapper userCampMapper;

    @Override
    public PriceLogPageResponseVO getPage(PriceLogPageRequest request, Long userId) {
        QueryContext context = buildQueryContext(request, userId);
        List<PriceLogRowVO> rows = filterAndHydrateRows(context);
        PageSlice<PriceLogRowVO> pageSlice = pageSlice(rows, context.page(), context.pageSize());

        PriceLogPageResponseVO response = new PriceLogPageResponseVO();
        response.setList(pageSlice.items());
        response.setPagination(buildPagination(context.page(), context.pageSize(), pageSlice.total()));
        response.setDictionaries(buildDictionaries(context.campId()));
        return response;
    }

    @Override
    public PriceLogExportResponseVO export(PriceLogPageRequest request, Long userId) {
        QueryContext context = buildQueryContext(request, userId);
        List<PriceLogRowVO> rows = filterAndHydrateRows(context);

        PriceLogExportResponseVO response = new PriceLogExportResponseVO();
        response.setFileName("price_logs_" + resolveExportDate(context.adjustmentStart()) + ".csv");
        response.setContentType("text/csv");
        response.setTotal(rows.size());
        response.setRows(rows);
        return response;
    }

    private QueryContext buildQueryContext(PriceLogPageRequest request, Long userId) {
        Long campId = resolveAccessibleCampId(parseLong(request.getCampId()), userId);
        return new QueryContext(
                campId,
                trimToNull(request.getKeyword()),
                trimToNull(request.getAdjustType()),
                parseLong(request.getChannelId()),
                parseDate(request.getAdjustmentStart()),
                parseDate(request.getAdjustmentEnd()),
                parseDate(request.getOperationStart()),
                parseDate(request.getOperationEnd()),
                trimToNull(request.getOperator()),
                normalizePage(request.getPage() == null ? request.getPageNum() : request.getPage()),
                normalizePageSize(request.getPageSize())
        );
    }

    private List<PriceLogRowVO> filterAndHydrateRows(QueryContext context) {
        List<PriceLogRowVO> rows = priceLogMapper.selectRows(
                context.campId(),
                context.keyword(),
                context.channelId(),
                context.adjustmentStart(),
                context.adjustmentEnd(),
                toStartTime(context.operationStart()),
                toEndExclusiveTime(context.operationEnd())
        );

        rows.forEach(this::hydrateDerivedFields);
        return rows.stream()
                .filter(row -> matchesAdjustType(row, context.adjustType()))
                .filter(row -> matchesOperator(row, context.operator()))
                .sorted(Comparator
                        .comparing(PriceLogRowVO::getOperationTime, Comparator.nullsLast(String::compareTo))
                        .thenComparing(PriceLogRowVO::getLogId, Comparator.nullsLast(String::compareTo))
                        .reversed())
                .toList();
    }

    private void hydrateDerivedFields(PriceLogRowVO row) {
        row.setLogId("PL" + row.getSnapshotId());
        row.setChannelName(resolveChannelName(row));
        row.setAdjustTypeName(resolveAdjustTypeName(row));
        row.setOperatorName(SYSTEM_OPERATOR_NAME);
        row.setActionContent(resolveActionContent(row));
    }

    private String resolveChannelName(PriceLogRowVO row) {
        if (row.getChannelId() == null || row.getChannelId() == 0L) {
            return SELF_CHANNEL_NAME;
        }
        if (row.getChannelName() == null || row.getChannelName().isBlank()) {
            return "Channel-" + row.getChannelId();
        }
        return row.getChannelName();
    }

    private String resolveAdjustTypeName(PriceLogRowVO row) {
        if (row.getChannelId() != null && row.getChannelId() > 0) {
            return SYSTEM_ADJUST_TYPE_NAME;
        }
        return MANUAL_ADJUST_TYPE_NAME;
    }

    private String resolveActionContent(PriceLogRowVO row) {
        String priceTypeName = resolvePriceTypeName(row.getPriceType());
        Long previousPriceCent = row.getPreviousPriceCent();
        if (previousPriceCent != null) {
            return priceTypeName + "\u4ece " + formatYuan(previousPriceCent) + " \u8c03\u6574\u4e3a " + formatYuan(row.getChannelSalePrice());
        }
        return priceTypeName + "\u8bbe\u7f6e\u4e3a " + formatYuan(row.getChannelSalePrice());
    }

    private String resolvePriceTypeName(String priceType) {
        if (priceType == null) {
            return "\u623f\u4ef7";
        }
        return switch (priceType.toLowerCase(Locale.ROOT)) {
            case "retail" -> "\u96f6\u552e\u4ef7";
            case "channel" -> "\u6e20\u9053\u4ef7";
            case "central" -> "\u4e2d\u53f0\u4ef7";
            case "price_board" -> "\u7535\u5b50\u623f\u4ef7\u724c";
            default -> "\u623f\u4ef7";
        };
    }

    private boolean matchesAdjustType(PriceLogRowVO row, String adjustType) {
        if (adjustType == null || adjustType.isBlank()) {
            return true;
        }
        return switch (adjustType) {
            case MANUAL_ADJUST_TYPE -> MANUAL_ADJUST_TYPE_NAME.equals(row.getAdjustTypeName());
            case SYSTEM_ADJUST_TYPE -> SYSTEM_ADJUST_TYPE_NAME.equals(row.getAdjustTypeName());
            default -> true;
        };
    }

    private boolean matchesOperator(PriceLogRowVO row, String operator) {
        if (operator == null || operator.isBlank()) {
            return true;
        }
        return row.getOperatorName() != null && row.getOperatorName().contains(operator);
    }

    private PriceLogDictionariesVO buildDictionaries(Long campId) {
        List<PriceLogOptionVO> channels = new ArrayList<>();
        channels.add(option(SELF_CHANNEL_NAME, "0"));
        priceLogMapper.selectChannelOptions(campId).stream()
                .filter(option -> option.getValue() != null && !"0".equals(option.getValue()))
                .filter(option -> channels.stream().noneMatch(existing -> Objects.equals(existing.getValue(), option.getValue())))
                .forEach(channels::add);

        PriceLogDictionariesVO dictionaries = new PriceLogDictionariesVO();
        dictionaries.setChannels(channels);
        dictionaries.setAdjustmentModes(List.of(
                option(MANUAL_ADJUST_TYPE_NAME, MANUAL_ADJUST_TYPE),
                option(SYSTEM_ADJUST_TYPE_NAME, SYSTEM_ADJUST_TYPE)
        ));
        return dictionaries;
    }

    private PriceLogOptionVO option(String label, String value) {
        PriceLogOptionVO option = new PriceLogOptionVO();
        option.setLabel(label);
        option.setValue(value);
        return option;
    }

    private PriceLogPaginationVO buildPagination(int page, int pageSize, long total) {
        PriceLogPaginationVO pagination = new PriceLogPaginationVO();
        pagination.setPage(page);
        pagination.setPageSize(pageSize);
        pagination.setTotal(total);
        return pagination;
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        Long currentCampId = userCampMapper.selectCurrentCampId(userId);
        if (currentCampId == null) {
            throw new BusinessException(40401, "\u672a\u627e\u5230\u5f53\u524d\u7528\u6237\u95e8\u5e97");
        }
        if (requestedCampId == null) {
            return currentCampId;
        }
        if (!requestedCampId.equals(currentCampId)) {
            throw new BusinessException(40301, "\u65e0\u6743\u8bbf\u95ee\u5f53\u524d\u95e8\u5e97\u8c03\u4ef7\u65e5\u5fd7");
        }
        return requestedCampId;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank() || "ALL".equalsIgnoreCase(value) || "all".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value, DATE_FORMATTER);
    }

    private LocalDateTime toStartTime(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }

    private LocalDateTime toEndExclusiveTime(LocalDate date) {
        return date == null ? null : date.plusDays(1).atStartOfDay();
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String formatYuan(Long cent) {
        long safeCent = cent == null ? 0L : cent;
        return BigDecimal.valueOf(safeCent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .toPlainString();
    }

    private String resolveExportDate(LocalDate adjustmentStart) {
        if (adjustmentStart == null) {
            return LocalDate.now(SHANGHAI_ZONE).format(DATE_FORMATTER);
        }
        return adjustmentStart.format(DATE_FORMATTER);
    }

    private <T> PageSlice<T> pageSlice(List<T> items, int page, int pageSize) {
        long total = items.size();
        int fromIndex = Math.min((page - 1) * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return new PageSlice<>(total, items.subList(fromIndex, toIndex));
    }

    private record QueryContext(
            Long campId,
            String keyword,
            String adjustType,
            Long channelId,
            LocalDate adjustmentStart,
            LocalDate adjustmentEnd,
            LocalDate operationStart,
            LocalDate operationEnd,
            String operator,
            int page,
            int pageSize
    ) {
    }

    private record PageSlice<T>(long total, List<T> items) {
    }
}
