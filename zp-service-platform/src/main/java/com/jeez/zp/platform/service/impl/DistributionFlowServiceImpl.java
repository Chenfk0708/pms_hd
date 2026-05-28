package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.DistributionFlowMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.DistributionFlowService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.DistributionOrderCampVO;
import com.jeez.zp.platform.vo.DistributionOrderPageResponseVO;
import com.jeez.zp.platform.vo.DistributionOrderPaginationVO;
import com.jeez.zp.platform.vo.DistributionOrderSummaryVO;
import com.jeez.zp.platform.vo.DistributionFlowPageResponseVO;
import com.jeez.zp.platform.vo.DistributionFlowRowVO;
import com.jeez.zp.platform.vo.DistributionFlowQueryRowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DistributionFlowServiceImpl implements DistributionFlowService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final DistributionFlowMapper distributionFlowMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public DistributionFlowPageResponseVO getPage(
            Long campId,
            Long userId,
            Integer pageNum,
            Integer current,
            Integer pageSize,
            String bookingStartDate,
            String bookingEndDate,
            String keyword,
            Boolean breakTemp,
            String settledState
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = normalizePageNum(pageNum, current);
        int resolvedPageSize = normalizePageSize(pageSize);
        long offset = (long) (resolvedPageNum - 1) * resolvedPageSize;

        LocalDateTime bookingStart = parseDateStart(bookingStartDate);
        LocalDateTime bookingEndExclusive = parseDateEndExclusive(bookingEndDate);

        long total = distributionFlowMapper.countPage(
                resolvedCampId,
                bookingStart,
                bookingEndExclusive,
                trimToNull(keyword),
                breakTemp,
                normalizeSettledState(settledState)
        );

        List<DistributionFlowRowVO> list = total == 0
                ? List.of()
                : distributionFlowMapper.selectPage(
                        resolvedCampId,
                        bookingStart,
                        bookingEndExclusive,
                        trimToNull(keyword),
                        breakTemp,
                        normalizeSettledState(settledState),
                        offset,
                        resolvedPageSize
                ).stream().map(this::toRow).toList();

        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / resolvedPageSize);

        DistributionFlowPageResponseVO response = new DistributionFlowPageResponseVO();
        response.setTotal(total);
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setPageNum(resolvedPageNum);
        response.setPages(pages);
        response.setHasNextPage(resolvedPageNum < pages);
        response.setList(list);
        return response;
    }

    @Override
    public DistributionOrderPageResponseVO getDistributionOrdersPage(
            Long campId,
            Long userId,
            Integer pageNum,
            Integer current,
            Integer pageSize,
            String bookingStartDate,
            String bookingEndDate,
            String keyword,
            String settledState
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = normalizePageNum(pageNum, current);
        int resolvedPageSize = normalizePageSize(pageSize);
        long offset = (long) (resolvedPageNum - 1) * resolvedPageSize;
        LocalDateTime bookingStart = parseDateStart(bookingStartDate);
        LocalDateTime bookingEndExclusive = parseDateEndExclusive(bookingEndDate);
        String normalizedSettledState = normalizeSettledState(settledState);

        long total = distributionFlowMapper.countPage(
                resolvedCampId,
                bookingStart,
                bookingEndExclusive,
                trimToNull(keyword),
                null,
                normalizedSettledState
        );
        List<DistributionFlowRowVO> list = total == 0
                ? List.of()
                : distributionFlowMapper.selectPage(
                resolvedCampId,
                bookingStart,
                bookingEndExclusive,
                trimToNull(keyword),
                null,
                normalizedSettledState,
                offset,
                resolvedPageSize
        ).stream().map(this::toRow).toList();

        DistributionOrderPageResponseVO response = new DistributionOrderPageResponseVO();
        response.setCamp(toDistributionOrderCamp(resolvedCampId));
        response.setList(list);
        response.setSummary(toDistributionOrderSummary(list));
        response.setPagination(toDistributionOrderPagination(resolvedPageNum, resolvedPageSize, total));
        return response;
    }

    private DistributionFlowRowVO toRow(DistributionFlowQueryRowVO row) {
        DistributionFlowRowVO item = new DistributionFlowRowVO();
        item.setOrderNo(defaultString(row.getOrderNo()));
        item.setOrderId(defaultString(row.getOrderNo()));
        item.setCustomerName(defaultString(row.getCustomerName()));
        item.setCustomerPhone(defaultString(row.getCustomerPhone()));
        item.setCustomerInfo(customerInfo(row));
        item.setRoomCategoryName(defaultString(row.getRoomCategoryName()));
        item.setBookedTime(defaultString(row.getBookedTime()));
        item.setBookedTimeStr(defaultString(row.getBookedTime()));
        item.setInvoicePrice(toAmount(row.getInvoicePriceCent()));
        item.setCommission(toAmount(row.getCommissionCent()));
        item.setIncomePrice(toAmount(row.getIncomePriceCent()));
        item.setSettledPrice(toAmount(row.getSettledPriceCent()));
        item.setPaidAmount(toAmount(row.getInvoicePriceCent()));
        item.setServiceFee(toAmount(row.getCommissionCent()));
        item.setSettlementAmount(toAmount(row.getIncomePriceCent()));
        item.setSettledAmount(toAmount(row.getSettledPriceCent()));
        item.setSettledState(defaultString(row.getSettledState()));
        item.setSettlementStatus("settled".equalsIgnoreCase(defaultString(row.getSettledState())) ? "已结算" : "待结算");
        item.setOrderFilter(Boolean.TRUE.equals(row.getBreakTemp()) ? "置换订单" : "非置换订单");
        return item;
    }

    private String customerInfo(DistributionFlowQueryRowVO row) {
        String customerName = defaultString(row.getCustomerName());
        String customerPhone = defaultString(row.getCustomerPhone());
        if (customerPhone.isBlank()) {
            return customerName;
        }
        if (customerName.isBlank()) {
            return customerPhone;
        }
        return customerName + "/" + customerPhone;
    }

    private double toAmount(Long centValue) {
        if (centValue == null) {
            return 0D;
        }
        return BigDecimal.valueOf(centValue)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private DistributionOrderCampVO toDistributionOrderCamp(Long campId) {
        DistributionOrderCampVO camp = new DistributionOrderCampVO();
        camp.setCampId(String.valueOf(campId));
        camp.setCampName(defaultString(distributionFlowMapper.selectCampName(campId)));
        return camp;
    }

    private DistributionOrderSummaryVO toDistributionOrderSummary(List<DistributionFlowRowVO> rows) {
        DistributionOrderSummaryVO summary = new DistributionOrderSummaryVO();
        summary.setInvoicePrice(roundAmount(rows.stream().mapToDouble(row -> safeAmount(row.getInvoicePrice())).sum()));
        summary.setCommission(roundAmount(rows.stream().mapToDouble(row -> safeAmount(row.getCommission())).sum()));
        summary.setIncomePrice(roundAmount(rows.stream().mapToDouble(row -> safeAmount(row.getIncomePrice())).sum()));
        summary.setSettledPrice(roundAmount(rows.stream().mapToDouble(row -> safeAmount(row.getSettledPrice())).sum()));
        return summary;
    }

    private DistributionOrderPaginationVO toDistributionOrderPagination(int pageNum, int pageSize, long total) {
        DistributionOrderPaginationVO pagination = new DistributionOrderPaginationVO();
        pagination.setPage(pageNum);
        pagination.setPageSize(pageSize);
        pagination.setTotal(total);
        return pagination;
    }

    private double safeAmount(Double value) {
        return value == null ? 0D : value;
    }

    private double roundAmount(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private int normalizePageNum(Integer pageNum, Integer current) {
        Integer candidate = pageNum != null ? pageNum : current;
        return candidate == null || candidate < 1 ? DEFAULT_PAGE_NUM : candidate;
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

    private String normalizeSettledState(String settledState) {
        if ("settled".equalsIgnoreCase(defaultString(settledState))) {
            return "settled";
        }
        if ("pending".equalsIgnoreCase(defaultString(settledState))) {
            return "pending";
        }
        return null;
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
            throw new BusinessException(40301, "无权访问当前门店分销订单数据");
        }
        return requestedCampId;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
