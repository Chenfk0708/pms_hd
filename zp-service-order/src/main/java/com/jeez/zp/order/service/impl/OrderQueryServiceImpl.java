package com.jeez.zp.order.service.impl;

import com.jeez.zp.order.exception.BusinessException;
import com.jeez.zp.order.mapper.OrderQueryMapper;
import com.jeez.zp.order.mapper.UserCampMapper;
import com.jeez.zp.order.service.OrderQueryService;
import com.jeez.zp.order.service.OrderSensitiveDataCipher;
import com.jeez.zp.order.vo.OrderDetailViewVO;
import com.jeez.zp.order.vo.LongRentalOrderPageItemVO;
import com.jeez.zp.order.vo.OrderDetailAggregateVO;
import com.jeez.zp.order.vo.OrderDetailRowVO;
import com.jeez.zp.order.vo.OrderGuestVO;
import com.jeez.zp.order.vo.OrderPageItemVO;
import com.jeez.zp.order.vo.OrderPageResponseVO;
import com.jeez.zp.order.vo.OrderPaymentRecordRowVO;
import com.jeez.zp.order.vo.OrderPaymentRecordVO;
import com.jeez.zp.order.vo.OrderQueryRowVO;
import com.jeez.zp.order.vo.StrongReminderItemVO;
import com.jeez.zp.order.vo.StrongReminderPageResponseVO;
import com.jeez.zp.order.vo.StrongReminderPaginationVO;
import com.jeez.zp.order.vo.StrongReminderRowVO;
import com.jeez.zp.order.vo.WorkspaceOrderItemVO;
import com.jeez.zp.order.vo.WorkspaceOrderListRowVO;
import com.jeez.zp.order.vo.WorkspaceOrdersResponseVO;
import com.jeez.zp.order.vo.WorkspacePaginationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final OrderQueryMapper orderQueryMapper;
    private final UserCampMapper userCampMapper;
    private final OrderSensitiveDataCipher sensitiveDataCipher;

    @Override
    public OrderPageResponseVO getHousePage(
            Long campId,
            Long userId,
            Integer pageNum,
            Integer pageSize,
            String orderType,
            String searchContent,
            String keyword
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int resolvedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        String normalizedKeyword = firstNonBlank(searchContent, keyword);
        String normalizedOrderType = trimToNull(orderType);

        List<OrderQueryRowVO> filtered = orderQueryMapper.selectHouseOrders(resolvedCampId, normalizedKeyword).stream()
                .filter(row -> matchesOrderType(row, normalizedOrderType, LocalDate.now(SHANGHAI_ZONE)))
                .toList();

        PageSlice<OrderPageItemVO> pageSlice = pageSlice(filtered.stream().map(this::toPageItem).toList(), resolvedPageNum, resolvedPageSize);

        OrderPageResponseVO response = new OrderPageResponseVO();
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setPageNum(resolvedPageNum);
        response.setPageSize(resolvedPageSize);
        response.setTotal(pageSlice.total());
        response.setHasNextPage(resolvedPageNum < pageSlice.pages());
        response.setPages(pageSlice.pages());
        response.setList(pageSlice.items());
        return response;
    }

    @Override
    public WorkspaceOrdersResponseVO getWorkspaceOrders(
            Long campId,
            Long userId,
            String orderType,
            Integer page,
            Integer pageNum,
            Integer current,
            Integer pageSize,
            String keyword
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = normalizePageNum(page, pageNum, current);
        int resolvedPageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        LocalDate today = LocalDate.now(SHANGHAI_ZONE);

        List<WorkspaceOrderListRowVO> filtered = orderQueryMapper.selectWorkspaceOrders(resolvedCampId, trimToNull(keyword)).stream()
                .filter(row -> matchesWorkspaceOrderType(row, orderType, today))
                .toList();

        int fromIndex = Math.min((resolvedPageNum - 1) * resolvedPageSize, filtered.size());
        int toIndex = Math.min(fromIndex + resolvedPageSize, filtered.size());

        WorkspaceOrdersResponseVO response = new WorkspaceOrdersResponseVO();
        response.setTotal((long) filtered.size());
        response.setList(filtered.subList(fromIndex, toIndex).stream()
                .map(row -> toWorkspaceOrderItem(row, today))
                .toList());
        response.setPagination(toPagination(resolvedPageNum, resolvedPageSize, (long) filtered.size()));
        return response;
    }

    @Override
    public OrderPageResponseVO getLongRentalPage(
            Long campId,
            Long userId,
            Integer pageNum,
            Integer pageSize,
            String orderType,
            String searchContent,
            String keyword,
            String searchCode,
            String orderStatus,
            Long channelId,
            Long roomCategoryId,
            String liveStatus,
            Long poiId
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int resolvedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        String normalizedKeyword = firstNonBlank(searchContent, keyword, searchCode);

        List<OrderQueryRowVO> filtered = orderQueryMapper.selectHouseOrders(resolvedCampId, normalizedKeyword).stream()
                .filter(row -> matchesLongRentalOrderType(row, orderType))
                .filter(row -> matchesLongRentalOrderStatus(row, orderStatus))
                .filter(row -> matchesLongRentalLiveStatus(row, liveStatus))
                .filter(row -> channelId == null || channelId.toString().equals(row.getChannelId()))
                .filter(row -> roomCategoryId == null || roomCategoryId.toString().equals(row.getRoomCategoryId()))
                .filter(row -> poiId == null || poiId.toString().equals(row.getPoiId()))
                .sorted(Comparator
                        .comparingInt(this::longRentalSortWeight)
                        .thenComparing(OrderQueryRowVO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(OrderQueryRowVO::getOrderId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        PageSlice<LongRentalOrderPageItemVO> pageSlice = pageSlice(
                filtered.stream().map(this::toLongRentalPageItem).toList(),
                resolvedPageNum,
                resolvedPageSize
        );

        OrderPageResponseVO response = new OrderPageResponseVO();
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setPageNum(resolvedPageNum);
        response.setPageSize(resolvedPageSize);
        response.setTotal(pageSlice.total());
        response.setHasNextPage(resolvedPageNum < pageSlice.pages());
        response.setPages(pageSlice.pages());
        response.setList(pageSlice.items());
        return response;
    }

    @Override
    public OrderDetailAggregateVO getOrderDetail(Long campId, Long userId, Long orderId) {
        if (orderId == null) {
            throw new BusinessException(40001, "orderId is required");
        }
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        OrderDetailRowVO row = orderQueryMapper.selectOrderDetail(resolvedCampId, orderId);
        if (row == null) {
            throw new BusinessException(40401, "订单不存在");
        }

        OrderDetailAggregateVO detail = new OrderDetailAggregateVO();
        detail.setOrderId(row.getOrderId());
        detail.setOrderNo(row.getOrderNo());
        detail.setOutOrderId(row.getOutOrderId());
        detail.setOrderType(row.getOrderType());
        detail.setStatus(row.getStatus());
        detail.setStatusName(resolveDetailStatusName(row));
        detail.setPaymentStatus(row.getPaymentStatus());
        detail.setChannelId(row.getChannelId());
        detail.setChannelName(row.getChannelName());
        detail.setGuestName(row.getGuestName());
        detail.setGuestMobile(row.getGuestMobile());
        detail.setPoiId(row.getPoiId());
        detail.setPoiName(row.getPoiName());
        detail.setRoomCategoryId(row.getRoomCategoryId());
        detail.setRoomCategoryName(row.getRoomCategoryName());
        detail.setRoomId(row.getRoomId());
        detail.setRoomName(row.getRoomName());
        detail.setCheckInTime(formatDateTime(row.getStartAt()));
        detail.setCheckOutTime(formatDateTime(row.getEndAt()));
        detail.setDayNum(row.getDayNum());
        detail.setTotalPrice(defaultLong(row.getTotalPriceCent()));
        detail.setTotalPayPrice(defaultLong(row.getTotalPayPriceCent()));
        detail.setRefundPrice(defaultLong(row.getRefundPriceCent()));
        detail.setCommissionPrice(defaultLong(row.getCommissionPriceCent()));
        detail.setDebtPrice(Math.max(defaultLong(row.getTotalPriceCent()) - defaultLong(row.getTotalPayPriceCent()), 0L));
        detail.setPaymentWayId(row.getPaymentWayId());
        detail.setPaymentWayName(row.getPaymentWayName());
        detail.setRemark(row.getRemark());
        detail.setCreatedAt(formatDateTime(row.getCreatedAt()));
        detail.setGuests(orderQueryMapper.selectOrderGuests(orderId).stream()
                .map(this::decryptGuestIdCard)
                .toList());
        detail.setPaymentRecords(orderQueryMapper.selectOrderPaymentRecords(resolvedCampId, orderId).stream()
                .map(this::toPaymentRecord)
                .toList());
        return detail;
    }

    @Override
    public StrongReminderPageResponseVO getStrongReminderPage(
            Long campId,
            Long userId,
            Integer page,
            Integer pageNum,
            Integer current,
            Integer pageSize,
            String keyword
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = normalizePageNum(page, pageNum, current);
        int resolvedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;

        PageSlice<StrongReminderItemVO> pageSlice = pageSlice(
                orderQueryMapper.selectStrongReminderRows(resolvedCampId, trimToNull(keyword)).stream()
                        .map(this::toStrongReminderItem)
                        .toList(),
                resolvedPageNum,
                resolvedPageSize
        );

        StrongReminderPaginationVO pagination = new StrongReminderPaginationVO();
        pagination.setPageNum(resolvedPageNum);
        pagination.setPageSize(resolvedPageSize);
        pagination.setTotal(pageSlice.total());

        StrongReminderPageResponseVO response = new StrongReminderPageResponseVO();
        response.setList(pageSlice.items());
        response.setPagination(pagination);
        return response;
    }

    private boolean matchesOrderType(OrderQueryRowVO row, String orderType, LocalDate today) {
        if (orderType == null) {
            return true;
        }
        return switch (orderType) {
            case "1" -> isCreatedOn(row, today);
            case "11" -> isBookedFor(row, today);
            case "10" -> isStayingOn(row, today);
            case "12" -> isCheckingOutOn(row, today);
            case "4" -> isBookedFor(row, today.plusDays(1));
            case "5" -> isCheckingOutOn(row, today.plusDays(1));
            case "6" -> hasStatus(row, "pending");
            case "7" -> hasStatus(row, "refunding");
            case "8" -> isExceptionOrder(row);
            default -> true;
        };
    }

    private boolean matchesWorkspaceOrderType(WorkspaceOrderListRowVO row, String orderType, LocalDate today) {
        String normalized = trimToNull(orderType);
        if (normalized == null) {
            return true;
        }
        return switch (normalized) {
            case "11" -> isBooked(row) && overlapsDate(row.getStartAt(), row.getEndAt(), today);
            case "12" -> isStaying(row, today);
            case "13" -> isCheckingOut(row, today);
            default -> false;
        };
    }

    private boolean matchesLongRentalOrderType(OrderQueryRowVO row, String orderType) {
        String normalized = trimToNull(orderType);
        return "long_rental".equalsIgnoreCase(trimToNull(row.getOrderType()))
                && (normalized == null || "11".equals(normalized));
    }

    private boolean matchesLongRentalOrderStatus(OrderQueryRowVO row, String orderStatus) {
        String normalized = trimToNull(orderStatus);
        return normalized == null || normalized.equals(String.valueOf(resolveLongRentalOrderState(row)));
    }

    private boolean matchesLongRentalLiveStatus(OrderQueryRowVO row, String liveStatus) {
        String normalized = trimToNull(liveStatus);
        return normalized == null || normalized.equals(resolveLongRentalLiveStatusCode(row));
    }

    private boolean isCreatedOn(OrderQueryRowVO row, LocalDate date) {
        return date.equals(toLocalDate(row.getCreatedAt()));
    }

    private boolean isBookedFor(OrderQueryRowVO row, LocalDate date) {
        return hasStatus(row, "booked") && date.equals(toLocalDate(row.getStartAt()));
    }

    private boolean isStayingOn(OrderQueryRowVO row, LocalDate date) {
        LocalDate startDate = toLocalDate(row.getStartAt());
        LocalDate endDate = toLocalDate(row.getEndAt());
        return hasStatus(row, "checked_in")
                && startDate != null
                && endDate != null
                && !startDate.isAfter(date)
                && endDate.isAfter(date);
    }

    private boolean isCheckingOutOn(OrderQueryRowVO row, LocalDate date) {
        return hasStatus(row, "checked_in") && date.equals(toLocalDate(row.getEndAt()));
    }

    private boolean isExceptionOrder(OrderQueryRowVO row) {
        return hasStatus(row, "refunding") || hasStatus(row, "cancelled") || hasStatus(row, "refunded");
    }

    private boolean isBooked(WorkspaceOrderListRowVO row) {
        String status = trimToNull(row.getOrderStatus());
        return "booked".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status);
    }

    private boolean isCheckedIn(WorkspaceOrderListRowVO row) {
        return "checked_in".equalsIgnoreCase(trimToNull(row.getOrderStatus()));
    }

    private boolean isSameDate(LocalDateTime value, LocalDate targetDate) {
        return value != null && value.toLocalDate().isEqual(targetDate);
    }

    private boolean overlapsDate(LocalDateTime startAt, LocalDateTime endAt, LocalDate targetDate) {
        return startAt != null
                && endAt != null
                && startAt.isBefore(targetDate.plusDays(1).atStartOfDay())
                && endAt.isAfter(targetDate.atStartOfDay());
    }

    private boolean isStaying(WorkspaceOrderListRowVO row, LocalDate today) {
        if (!isCheckedIn(row) || row.getStartAt() == null || row.getEndAt() == null) {
            return false;
        }
        LocalDate startDate = row.getStartAt().toLocalDate();
        LocalDate endDate = row.getEndAt().toLocalDate();
        return !startDate.isAfter(today) && endDate.isAfter(today);
    }

    private boolean isCheckingOut(WorkspaceOrderListRowVO row, LocalDate today) {
        return isCheckedIn(row) && isSameDate(row.getEndAt(), today);
    }

    private OrderPageItemVO toPageItem(OrderQueryRowVO row) {
        long commissionPrice = defaultLong(row.getCommissionPriceCent());
        long totalRoomPrice = defaultLong(row.getTotalPriceCent());
        long includeCommissionRoomPrice = totalRoomPrice + commissionPrice;
        long otherPrice = defaultLong(row.getOtherPriceCent());

        OrderDetailViewVO detailView = new OrderDetailViewVO();
        detailView.setPoiId(row.getPoiId());
        detailView.setPoiName(row.getPoiName());
        detailView.setRoomCategoryId(row.getRoomCategoryId());
        detailView.setRoomCategoryName(row.getRoomCategoryName());
        detailView.setRoomCategoryProductName(row.getRoomCategoryProductName());
        detailView.setRoomId(row.getRoomId());
        detailView.setRoomName(row.getRoomName());
        detailView.setCheckInDate(toEpochMillis(row.getStartAt()));
        detailView.setCheckOutDate(toEpochMillis(row.getEndAt()));
        detailView.setOrderDetailDisplayState(resolveDetailDisplayState(row));
        detailView.setIsArrangeRoom(isBlank(row.getRoomName()) ? 0 : 1);
        detailView.setIsOccupation(isBlank(row.getRoomName()) ? 0 : 1);
        detailView.setIsStatistics(hasStatus(row, "cancelled") ? 0 : 1);

        OrderPageItemVO item = new OrderPageItemVO();
        item.setOrderId(row.getOrderId());
        item.setOutOrderId(row.getOutOrderId());
        item.setChannelId(row.getChannelId());
        item.setChannelName(row.getChannelName());
        item.setOrderChannelName(row.getOrderChannelName());
        item.setGuestName(row.getGuestName());
        item.setGuestMobile(row.getGuestMobile());
        item.setOrderState(resolveOrderState(row));
        item.setRefundDisplayState(resolveRefundDisplayState(row));
        item.setTotalRoomPrice(totalRoomPrice);
        item.setOtherPrice(otherPrice);
        item.setIncludeCommissionRoomPrice(includeCommissionRoomPrice);
        item.setOrderTotalIncomePrice(includeCommissionRoomPrice + otherPrice);
        item.setTotalPayPrice(defaultLong(row.getTotalPayPriceCent()));
        item.setCommissionPrice(commissionPrice);
        item.setDebtPrice(Math.max(defaultLong(row.getTotalPriceCent()) - defaultLong(row.getTotalPayPriceCent()), 0L));
        item.setBookedTime(toEpochMillis(row.getCreatedAt()));
        item.setCreateTime(toEpochMillis(row.getCreatedAt()));
        item.setConfirmNo(row.getConfirmNo());
        item.setOrderDetailViews(List.of(detailView));
        return item;
    }

    private WorkspaceOrderItemVO toWorkspaceOrderItem(WorkspaceOrderListRowVO row, LocalDate today) {
        String statusName = resolveWorkspaceOrderStatusName(row, today);

        WorkspaceOrderItemVO item = new WorkspaceOrderItemVO();
        item.setChannelName(defaultString(row.getChannelName(), "未知渠道"));
        item.setGuestName(defaultString(row.getGuestName(), "-"));
        item.setGuestMobile(defaultString(row.getGuestMobile(), "-"));
        item.setRoomCategoryName(defaultString(row.getRoomCategoryName(), "-"));
        item.setRoomName(defaultString(row.getRoomName(), "-"));
        item.setStartTime(toEpochMillis(row.getStartAt()));
        item.setEndTime(toEpochMillis(row.getEndAt()));
        item.setDayNum(row.getDayNum() == null || row.getDayNum() < 1 ? 1 : row.getDayNum());
        item.setOrderDetailDisplayStateName(statusName);
        item.setStatusName(statusName);
        return item;
    }

    private String resolveWorkspaceOrderStatusName(WorkspaceOrderListRowVO row, LocalDate today) {
        if (isBooked(row) && overlapsDate(row.getStartAt(), row.getEndAt(), today)) {
            return "待入住";
        }
        if (isCheckingOut(row, today)) {
            return "待退房";
        }
        if (isCheckedIn(row)) {
            return "在住";
        }
        return defaultString(row.getOrderStatus(), "待确认");
    }

    private LongRentalOrderPageItemVO toLongRentalPageItem(OrderQueryRowVO row) {
        long totalRoomPrice = defaultLong(row.getTotalPriceCent());
        long commissionPrice = defaultLong(row.getCommissionPriceCent());
        long otherPrice = defaultLong(row.getOtherPriceCent());
        long depositPrice = defaultLong(row.getDepositPriceCent());
        long totalIncomePrice = totalRoomPrice + commissionPrice + otherPrice + depositPrice;

        LongRentalOrderPageItemVO item = new LongRentalOrderPageItemVO();
        item.setOrderId(row.getOrderId());
        item.setOutOrderId(row.getOutOrderId());
        item.setChannelId(row.getChannelId());
        item.setOrderChannelId(row.getChannelId());
        item.setChannelName(row.getChannelName());
        item.setGuestName(row.getGuestName());
        item.setGuestMobile(row.getGuestMobile());
        item.setRoomCategoryId(row.getRoomCategoryId());
        item.setRoomCategoryName(row.getRoomCategoryName());
        item.setRoomName(defaultString(row.getRoomName(), "-"));
        item.setPoiId(row.getPoiId());
        item.setPoiName(row.getPoiName());
        item.setCheckInTime(formatDateTime(row.getStartAt()));
        item.setCheckOutTime(formatDateTime(row.getEndAt()));
        item.setLiveStatusName(resolveLongRentalLiveStatusName(row));
        item.setLiveStatusCode(resolveLongRentalLiveStatusCode(row));
        item.setOrderState(resolveLongRentalOrderState(row));
        item.setOrderType("11");
        item.setLtGrossRevenuePrice(totalRoomPrice + commissionPrice);
        item.setLtGrossProceedPrice(totalRoomPrice);
        item.setLtOtherPrice(otherPrice);
        item.setLtDepositPrice(depositPrice);
        item.setOrderTotalIncomePrice(totalIncomePrice);
        item.setLtRentStartDate(toDateString(row.getStartAt()));
        item.setLtRentEndDate(toDateString(row.getEndAt()));
        item.setLtPeriodOfContract(resolveLongRentalContractTerm(row));
        item.setPaymentWayName(defaultString(row.getPaymentCycle(), row.getPaymentWayName()));
        item.setPaymentTime(toDateString(row.getCreatedAt()));
        item.setCreateTimeText(formatDateTime(row.getCreatedAt()));
        item.setIsOccupyStock(isBlank(row.getRoomName()) ? 0 : 1);
        item.setArrangeRoomStatusName(isBlank(row.getRoomName()) ? "未排房" : "已排房");
        item.setIncludeStatisticsName(isLongRentalCancelled(row) ? "不计入统计" : "计入统计");
        item.setContractNo(defaultString(row.getContractNo(), "HT-LR-" + row.getOrderId()));
        item.setNextPaymentAmount(defaultLong(row.getNextPaymentAmountCent()));
        item.setNextPaymentDate(row.getNextPaymentDate() == null ? toDateString(row.getEndAt()) : row.getNextPaymentDate().toLocalDate().toString());
        return item;
    }

    private StrongReminderItemVO toStrongReminderItem(StrongReminderRowVO row) {
        StrongReminderItemVO item = new StrongReminderItemVO();
        item.setId("order-" + row.getOrderId());
        item.setCampId(row.getCampId());
        item.setLevel(resolveStrongReminderLevel(row));
        item.setTitle(resolveStrongReminderTitle(row));
        item.setGuestName(defaultString(row.getGuestName(), "-"));
        item.setRoomName(resolveStrongReminderRoomName(row));
        item.setOrderNo(row.getOrderNo());
        item.setDueAt(formatDueAt(row.getStartAt()));
        item.setChannel(resolveBusinessChannel(row));
        item.setStatus("pending");
        item.setPrimaryAction("order");
        item.setSummary(resolveStrongReminderSummary(row));
        return item;
    }

    private OrderPaymentRecordVO toPaymentRecord(OrderPaymentRecordRowVO row) {
        OrderPaymentRecordVO item = new OrderPaymentRecordVO();
        item.setPaymentRecordId(row.getPaymentRecordId());
        item.setPaymentTypeId(row.getPaymentTypeId());
        item.setPaymentWayId(row.getPaymentWayId());
        item.setPaymentWayName(row.getPaymentWayName());
        item.setIsIncome(row.getIsIncome());
        item.setAmount(defaultLong(row.getAmount()));
        item.setDebtAmount(defaultLong(row.getDebtAmount()));
        item.setPaymentNo(row.getPaymentNo());
        item.setPaymentTime(formatDateTime(row.getPaymentTime()));
        item.setOperatorName(row.getOperatorName());
        item.setRemark(row.getRemark());
        return item;
    }

    private OrderGuestVO decryptGuestIdCard(OrderGuestVO guest) {
        guest.setGuestIdCard(sensitiveDataCipher.decryptIdCard(guest.getGuestIdCard()));
        return guest;
    }

    private String resolveDetailStatusName(OrderDetailRowVO row) {
        String status = trimToNull(row.getStatus());
        if ("booked".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status)) {
            return "待入住";
        }
        if ("checked_in".equalsIgnoreCase(status)) {
            return "入住中";
        }
        if ("completed".equalsIgnoreCase(status)) {
            return "已退房";
        }
        if ("cancelled".equalsIgnoreCase(status)) {
            return "已取消";
        }
        if ("refunding".equalsIgnoreCase(status)) {
            return "退款中";
        }
        if ("refunded".equalsIgnoreCase(status)) {
            return "已退款";
        }
        return defaultString(row.getStatus(), "未知");
    }

    private String resolveStrongReminderLevel(StrongReminderRowVO row) {
        if (isStrongReminderStatus(row, "pending") || isStrongReminderStatus(row, "refunding")) {
            return "high";
        }
        return "medium";
    }

    private String resolveStrongReminderTitle(StrongReminderRowVO row) {
        if (isStrongReminderStatus(row, "pending")) {
            return "待确认订单";
        }
        if (isStrongReminderStatus(row, "refunding")) {
            return "退款处理提醒";
        }
        return "异常订单提醒";
    }

    private String resolveStrongReminderSummary(StrongReminderRowVO row) {
        if (isStrongReminderStatus(row, "pending")) {
            return "订单待确认/待支付，请及时跟进客人入住信息。";
        }
        if (isStrongReminderStatus(row, "refunding")) {
            return "订单存在退款流程，请及时核对支付与退款状态。";
        }
        return "订单状态异常，请及时核对渠道与房态。";
    }

    private boolean isStrongReminderStatus(StrongReminderRowVO row, String expectedStatus) {
        return expectedStatus.equalsIgnoreCase(trimToNull(row.getStatus()));
    }

    private String resolveStrongReminderRoomName(StrongReminderRowVO row) {
        String roomName = trimToNull(row.getRoomName());
        if (roomName != null) {
            return roomName;
        }
        return defaultString(row.getRoomCategoryName(), "-");
    }

    private String resolveBusinessChannel(StrongReminderRowVO row) {
        String channelName = trimToNull(row.getChannelName());
        String channelId = trimToNull(row.getChannelId());
        if ("2".equals(channelId) || containsIgnoreCase(channelName, "meituan") || channelName != null && channelName.contains("美团")) {
            return "meituan";
        }
        return "ctrip";
    }

    private Integer resolveOrderState(OrderQueryRowVO row) {
        if (hasStatus(row, "pending")) {
            return 0;
        }
        if (hasStatus(row, "booked")) {
            return 2;
        }
        if (hasStatus(row, "checked_in")) {
            return 3;
        }
        if (hasStatus(row, "completed")) {
            return 4;
        }
        if (hasStatus(row, "cancelled")) {
            return 5;
        }
        if (hasStatus(row, "refunded")) {
            return 9;
        }
        return 2;
    }

    private Integer resolveDetailDisplayState(OrderQueryRowVO row) {
        if (hasStatus(row, "checked_in")) {
            return 2;
        }
        if (hasStatus(row, "completed")) {
            return 3;
        }
        if (hasStatus(row, "cancelled") || hasStatus(row, "refunded")) {
            return 4;
        }
        return 1;
    }

    private Integer resolveRefundDisplayState(OrderQueryRowVO row) {
        if (hasStatus(row, "refunding")) {
            return 1;
        }
        if (defaultLong(row.getRefundPriceCent()) > 0
                || hasStatus(row, "refunded")
                || "refunded".equalsIgnoreCase(trimToNull(row.getPaymentStatus()))) {
            return 2;
        }
        return 0;
    }

    private Integer resolveLongRentalOrderState(OrderQueryRowVO row) {
        if (isLongRentalCancelled(row)) {
            return 5;
        }
        if (hasStatus(row, "checked_in") || hasStatus(row, "completed")) {
            return 2;
        }
        return 1;
    }

    private String resolveLongRentalLiveStatusName(OrderQueryRowVO row) {
        if (isLongRentalCancelled(row)) {
            return "已取消";
        }
        if (hasStatus(row, "checked_in")) {
            return "入住中";
        }
        if (hasStatus(row, "completed")) {
            return "已退房";
        }
        return "待入住";
    }

    private String resolveLongRentalLiveStatusCode(OrderQueryRowVO row) {
        if (isLongRentalCancelled(row)) {
            return "cancelled";
        }
        if (hasStatus(row, "checked_in")) {
            return "living";
        }
        return "pending";
    }

    private boolean isLongRentalCancelled(OrderQueryRowVO row) {
        return hasStatus(row, "cancelled") || hasStatus(row, "refunded");
    }

    private int longRentalSortWeight(OrderQueryRowVO row) {
        if (hasStatus(row, "checked_in")) {
            return 0;
        }
        if (isLongRentalCancelled(row)) {
            return 2;
        }
        return 1;
    }

    private <T> PageSlice<T> pageSlice(List<T> items, int pageNum, int pageSize) {
        long total = items.size();
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
        int fromIndex = Math.min((pageNum - 1) * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return new PageSlice<>(total, pages, items.subList(fromIndex, toIndex));
    }

    private WorkspacePaginationVO toPagination(int pageNum, int pageSize, long total) {
        WorkspacePaginationVO pagination = new WorkspacePaginationVO();
        pagination.setPage(pageNum);
        pagination.setPageSize(pageSize);
        pagination.setTotal(total);
        return pagination;
    }

    private int normalizePageNum(Integer page, Integer pageNum, Integer current) {
        Integer candidate = page != null ? page : (pageNum != null ? pageNum : current);
        return candidate == null || candidate < 1 ? DEFAULT_PAGE_NUM : candidate;
    }

    private boolean hasStatus(OrderQueryRowVO row, String expectedStatus) {
        return expectedStatus.equalsIgnoreCase(trimToNull(row.getStatus()));
    }

    private LocalDate toLocalDate(LocalDateTime value) {
        return value == null ? null : value.toLocalDate();
    }

    private Long toEpochMillis(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZone(SHANGHAI_ZONE).toInstant().toEpochMilli();
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.format(DATE_TIME_FORMATTER);
    }

    private String formatDueAt(LocalDateTime value) {
        return value == null ? null : value.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String toDateString(LocalDateTime value) {
        return value == null ? null : value.toLocalDate().toString();
    }

    private String resolveLongRentalContractTerm(OrderQueryRowVO row) {
        LocalDate startDate = toLocalDate(row.getStartAt());
        LocalDate endDate = toLocalDate(row.getEndAt());
        if (startDate == null || endDate == null) {
            return null;
        }
        return Math.max(endDate.toEpochDay() - startDate.toEpochDay(), 1) + "天";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            String normalized = trimToNull(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String defaultString(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean containsIgnoreCase(String value, String fragment) {
        return value != null && value.toLowerCase().contains(fragment.toLowerCase());
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
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

    private record PageSlice<T>(long total, int pages, List<T> items) {
    }
}
