package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.StatementOrderMapper;
import com.jeez.zp.platform.service.StatementOrderService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.StatementOrderPageResponseVO;
import com.jeez.zp.platform.vo.StatementOrderQueryRowVO;
import com.jeez.zp.platform.vo.StatementOrderRowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StatementOrderServiceImpl implements StatementOrderService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final StatementOrderMapper statementOrderMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public Object getStatement(
            Long campId,
            Long userId,
            List<Long> poiIds,
            String bookingStartDate,
            String bookingEndDate,
            Boolean breakTemp,
            Integer pageNum,
            Integer current,
            Integer pageSize,
            String exportExcelMenuId
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<Long> resolvedPoiIds = sanitizePoiIds(poiIds);

        if (exportExcelMenuId != null && !exportExcelMenuId.isBlank()) {
            return buildExportUrl(resolvedCampId, resolvedPoiIds, bookingStartDate, bookingEndDate);
        }

        int resolvedPageNum = normalizePageNum(pageNum, current);
        int resolvedPageSize = normalizePageSize(pageSize);
        long offset = (long) (resolvedPageNum - 1) * resolvedPageSize;

        LocalDateTime bookingStart = parseDateStart(bookingStartDate);
        LocalDateTime bookingEndExclusive = parseDateEndExclusive(bookingEndDate);

        long total = statementOrderMapper.countPage(
                resolvedCampId,
                resolvedPoiIds,
                bookingStart,
                bookingEndExclusive,
                breakTemp
        );

        List<StatementOrderRowVO> list = total == 0
                ? List.of()
                : statementOrderMapper.selectPage(
                        resolvedCampId,
                        resolvedPoiIds,
                        bookingStart,
                        bookingEndExclusive,
                        breakTemp,
                        offset,
                        resolvedPageSize
                ).stream().map(this::toRow).toList();

        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / resolvedPageSize);

        StatementOrderPageResponseVO response = new StatementOrderPageResponseVO();
        response.setTotal(total);
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setPageNum(resolvedPageNum);
        response.setPages(pages);
        response.setHasNextPage(resolvedPageNum < pages);
        response.setList(list);
        return response;
    }

    private StatementOrderRowVO toRow(StatementOrderQueryRowVO row) {
        StatementOrderRowVO item = new StatementOrderRowVO();
        item.setOrderId(defaultString(row.getOrderId()));
        item.setCustomerInfo(defaultString(row.getCustomerInfo()));
        item.setProductType(defaultString(row.getProductType()));
        item.setProductName(defaultString(row.getProductName()));
        item.setBookingTime(defaultString(row.getBookingTime()));
        item.setChannelName(defaultString(row.getChannelName()));
        item.setPayableAmount(toAmount(row.getPayableAmountCent()));
        item.setPaidAmount(toAmount(row.getPaidAmountCent()));
        item.setDiscountAmount(toAmount(row.getDiscountAmountCent()));
        item.setRefundAmount(toAmount(row.getRefundAmountCent()));
        item.setPaymentFee(toAmount(row.getPaymentFeeCent()));
        item.setPlatformServiceFee(toAmount(row.getPlatformServiceFeeCent()));
        item.setDistributorCommission(toAmount(row.getDistributorCommissionCent()));
        item.setPaymentWayName(defaultString(row.getPaymentWayName()));
        item.setSettlementAmount(toAmount(row.getSettlementAmountCent()));
        return item;
    }

    private double toAmount(Long centValue) {
        if (centValue == null) {
            return 0D;
        }
        return BigDecimal.valueOf(centValue)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private int normalizePageNum(Integer pageNum, Integer current) {
        Integer candidate = pageNum != null ? pageNum : current;
        return candidate == null || candidate < 1 ? DEFAULT_PAGE_NUM : candidate;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private List<Long> sanitizePoiIds(List<Long> poiIds) {
        if (poiIds == null || poiIds.isEmpty()) {
            return List.of();
        }
        return poiIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private String buildExportUrl(Long campId, List<Long> poiIds, String bookingStartDate, String bookingEndDate) {
        String poiScope = poiIds.isEmpty()
                ? "all"
                : poiIds.stream().map(String::valueOf).reduce((left, right) -> left + "-" + right).orElse("all");
        String start = normalizeDateLabel(bookingStartDate, "all-start");
        String end = normalizeDateLabel(bookingEndDate, "all-end");
        return "/downloads/report-storer-statement/" + campId + "/" + poiScope + "/" + start + "_" + end + ".xlsx";
    }

    private String normalizeDateLabel(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return LocalDate.parse(value).toString();
        } catch (DateTimeParseException ex) {
            throw new BusinessException(40001, "日期格式错误");
        }
    }

    private LocalDateTime parseDateStart(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value).atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw new BusinessException(40001, "bookingStartDate格式错误");
        }
    }

    private LocalDateTime parseDateEndExclusive(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value).plusDays(1).atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw new BusinessException(40001, "bookingEndDate格式错误");
        }
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
            throw new BusinessException(40301, "无权访问当前门店品牌小程序订单报表");
        }
        return requestedCampId;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
