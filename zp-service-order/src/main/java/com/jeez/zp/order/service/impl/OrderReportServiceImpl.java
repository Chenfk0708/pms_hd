package com.jeez.zp.order.service.impl;

import com.jeez.zp.order.exception.BusinessException;
import com.jeez.zp.order.mapper.OrderReportMapper;
import com.jeez.zp.order.mapper.UserCampMapper;
import com.jeez.zp.order.service.OrderReportService;
import com.jeez.zp.order.vo.OrderReportRowVO;
import com.jeez.zp.order.vo.OrderReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class OrderReportServiceImpl implements OrderReportService {

    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");

    private final OrderReportMapper orderReportMapper;
    private final UserCampMapper userCampMapper;

    @Override
    public OrderReportVO getReport(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<OrderReportRowVO> rows = orderReportMapper.selectOrderReportRows(resolvedCampId);
        LocalDate today = LocalDate.now(SHANGHAI_ZONE);
        LocalDate tomorrow = today.plusDays(1);

        OrderReportVO report = new OrderReportVO();
        report.setTodayNewOrder(count(rows, row -> isCreatedOn(row, today)));
        report.setTodayPredictCheckIn(count(rows, row -> isBookedFor(row, today)));
        report.setStaying(count(rows, row -> isStayingOn(row, today)));
        report.setTodayPredictCheckOut(count(rows, row -> isCheckingOutOn(row, today)));
        report.setTomorrowCheckIn(count(rows, row -> isBookedFor(row, tomorrow)));
        report.setTomorrowCheckOut(count(rows, row -> isCheckingOutOn(row, tomorrow)));
        report.setPending(count(rows, row -> hasStatus(row, "pending")));
        report.setRefunding(count(rows, row -> hasStatus(row, "refunding")));
        report.setException(count(rows, this::isExceptionOrder));
        return report;
    }

    private boolean isCreatedOn(OrderReportRowVO row, LocalDate date) {
        return date.equals(toLocalDate(row.getCreatedAt()));
    }

    private boolean isBookedFor(OrderReportRowVO row, LocalDate date) {
        return hasStatus(row, "booked") && date.equals(toLocalDate(row.getStartAt()));
    }

    private boolean isStayingOn(OrderReportRowVO row, LocalDate date) {
        LocalDate startDate = toLocalDate(row.getStartAt());
        LocalDate endDate = toLocalDate(row.getEndAt());
        return hasStatus(row, "checked_in")
                && startDate != null
                && endDate != null
                && !startDate.isAfter(date)
                && endDate.isAfter(date);
    }

    private boolean isCheckingOutOn(OrderReportRowVO row, LocalDate date) {
        return hasStatus(row, "checked_in") && date.equals(toLocalDate(row.getEndAt()));
    }

    private boolean isExceptionOrder(OrderReportRowVO row) {
        return hasStatus(row, "refunding") || hasStatus(row, "cancelled") || hasStatus(row, "refunded");
    }

    private boolean hasStatus(OrderReportRowVO row, String expectedStatus) {
        return expectedStatus.equalsIgnoreCase(trimToNull(row.getStatus()));
    }

    private int count(List<OrderReportRowVO> rows, Predicate<OrderReportRowVO> predicate) {
        return Math.toIntExact(rows.stream().filter(predicate).count());
    }

    private LocalDate toLocalDate(LocalDateTime value) {
        return value == null ? null : value.toLocalDate();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
            throw new BusinessException(40301, "\u65E0\u6743\u8BBF\u95EE\u5F53\u524D\u95E8\u5E97\u8BA2\u5355\u6570\u636E");
        }
        return requestedCampId;
    }
}
