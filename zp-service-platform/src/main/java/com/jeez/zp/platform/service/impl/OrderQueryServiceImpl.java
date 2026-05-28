package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.OrderQueryMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.OrderQueryService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.HotelPackageOrderPageItemVO;
import com.jeez.zp.platform.vo.LongRentalOrderPageItemVO;
import com.jeez.zp.platform.vo.OrderDetailViewVO;
import com.jeez.zp.platform.vo.OrderPageItemVO;
import com.jeez.zp.platform.vo.OrderPageResponseVO;
import com.jeez.zp.platform.vo.OrderQueryRowVO;
import com.jeez.zp.platform.vo.OrderReportVO;
import com.jeez.zp.platform.vo.PresaleOrderDetailViewVO;
import com.jeez.zp.platform.vo.PresaleOrderPageItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PlatformBootstrapMapper platformBootstrapMapper;
    private final OrderQueryMapper orderQueryMapper;

    @Override
    public OrderReportVO getReport(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<OrderQueryRowVO> records = orderQueryMapper.selectHouseOrders(resolvedCampId, null);
        LocalDate today = LocalDate.now(SHANGHAI_ZONE);
        LocalDate tomorrow = today.plusDays(1);

        OrderReportVO report = new OrderReportVO();
        report.setTodayNewOrder(count(records, row -> isCreatedOn(row, today)));
        report.setTodayPredictCheckIn(count(records, row -> isBookedFor(row, today)));
        report.setStaying(count(records, row -> isStayingOn(row, today)));
        report.setTodayPredictCheckOut(count(records, row -> isCheckingOutOn(row, today)));
        report.setTomorrowCheckIn(count(records, row -> isBookedFor(row, tomorrow)));
        report.setTomorrowCheckOut(count(records, row -> isCheckingOutOn(row, tomorrow)));
        report.setPending(count(records, row -> hasStatus(row, "pending")));
        report.setRefunding(count(records, this::isRefunding));
        report.setException(count(records, this::isExceptionOrder));
        return report;
    }

    @Override
    public OrderPageResponseVO getPage(
            Long campId,
            Long userId,
            Integer pageNum,
            Integer pageSize,
            List<String> roomCategoryTypes,
            List<String> orderStates,
            List<String> categoryIds,
            List<String> orderChannelIds,
            List<String> paymentWayIds,
            String refundDisplayState,
            Long bookedStartDate,
            Long bookedEndDate,
            String orderType,
            Integer isLt,
            String searchContent,
            String keyword,
            String searchCode,
            String dateType,
            String orderStatus,
            Long channelId,
            Long roomCategoryId,
            String liveStatus,
            Long poiId
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int resolvedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        int normalizedIsLt = isLt == null ? 0 : isLt;

        OrderPageResponseVO response = new OrderPageResponseVO();
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setPageNum(resolvedPageNum);
        response.setPageSize(resolvedPageSize);

        if (containsValue(roomCategoryTypes, "4")) {
            fillHotelPackagePage(
                    response,
                    resolvedCampId,
                    resolvedPageNum,
                    resolvedPageSize,
                    roomCategoryTypes,
                    orderStates,
                    orderChannelIds,
                    paymentWayIds,
                    refundDisplayState,
                    bookedStartDate,
                    bookedEndDate,
                    searchContent,
                    keyword,
                    searchCode
            );
            return response;
        }

        if (isPresaleOrderQuery(roomCategoryTypes)) {
            fillPresalePage(
                    response,
                    resolvedCampId,
                    resolvedPageNum,
                    resolvedPageSize,
                    roomCategoryTypes,
                    orderStates,
                    categoryIds,
                    orderChannelIds,
                    paymentWayIds,
                    refundDisplayState,
                    bookedStartDate,
                    bookedEndDate,
                    searchContent,
                    keyword,
                    searchCode
            );
            return response;
        }

        if (normalizedIsLt != 0 && normalizedIsLt != 1) {
            throw new BusinessException(400, "\u6682\u4e0d\u652f\u6301\u5f53\u524d\u8ba2\u5355\u7c7b\u578b\u67e5\u8be2");
        }

        if (normalizedIsLt == 1) {
            fillLongRentalPage(
                    response,
                    resolvedCampId,
                    resolvedPageNum,
                    resolvedPageSize,
                    orderType,
                    searchContent,
                    keyword,
                    searchCode,
                    dateType,
                    orderStatus,
                    channelId,
                    roomCategoryId,
                    liveStatus,
                    poiId
            );
            return response;
        }

        fillHousePage(response, resolvedCampId, resolvedPageNum, resolvedPageSize, orderType, searchContent);
        return response;
    }

    private void fillHotelPackagePage(
            OrderPageResponseVO response,
            Long campId,
            int pageNum,
            int pageSize,
            List<String> roomCategoryTypes,
            List<String> orderStates,
            List<String> orderChannelIds,
            List<String> paymentWayIds,
            String refundDisplayState,
            Long bookedStartDate,
            Long bookedEndDate,
            String searchContent,
            String keyword,
            String searchCode
    ) {
        String normalizedKeyword = firstNonBlank(searchContent, keyword, searchCode);
        List<OrderQueryRowVO> records = orderQueryMapper.selectHouseOrders(campId, normalizedKeyword);
        List<OrderQueryRowVO> filtered = records.stream()
                .filter(this::isHotelPackageOrder)
                .filter(row -> matchesRoomCategoryTypes(row, roomCategoryTypes))
                .filter(row -> matchesHotelPackageOrderStates(row, orderStates))
                .filter(row -> matchesHotelPackageOrderChannels(row, orderChannelIds))
                .filter(row -> matchesHotelPackagePaymentWays(row, paymentWayIds))
                .filter(row -> matchesHotelPackageRefundDisplayState(row, refundDisplayState))
                .filter(row -> matchesHotelPackageBookedRange(row, bookedStartDate, bookedEndDate))
                .sorted(Comparator
                        .comparing(OrderQueryRowVO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(OrderQueryRowVO::getOrderId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        PageSlice<HotelPackageOrderPageItemVO> pageSlice = pageSlice(filtered.stream().map(this::toHotelPackagePageItem).toList(), pageNum, pageSize);
        fillPageResponse(response, pageNum, pageSlice);
    }

    private void fillPresalePage(
            OrderPageResponseVO response,
            Long campId,
            int pageNum,
            int pageSize,
            List<String> roomCategoryTypes,
            List<String> orderStates,
            List<String> categoryIds,
            List<String> orderChannelIds,
            List<String> paymentWayIds,
            String refundDisplayState,
            Long bookedStartDate,
            Long bookedEndDate,
            String searchContent,
            String keyword,
            String searchCode
    ) {
        String normalizedKeyword = firstNonBlank(searchContent, keyword, searchCode);
        List<OrderQueryRowVO> records = orderQueryMapper.selectHouseOrders(campId, normalizedKeyword);
        List<OrderQueryRowVO> filtered = records.stream()
                .filter(this::isPresaleOrder)
                .filter(row -> matchesRoomCategoryTypes(row, roomCategoryTypes))
                .filter(row -> matchesPresaleOrderStates(row, orderStates))
                .filter(row -> matchesPresaleCategories(row, categoryIds))
                .filter(row -> matchesPresaleOrderChannels(row, orderChannelIds))
                .filter(row -> matchesPresalePaymentWays(row, paymentWayIds))
                .filter(row -> matchesPresaleRefundDisplayState(row, refundDisplayState))
                .filter(row -> matchesPresaleBookedRange(row, bookedStartDate, bookedEndDate))
                .sorted(Comparator
                        .comparing(OrderQueryRowVO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(OrderQueryRowVO::getOrderId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        PageSlice<PresaleOrderPageItemVO> pageSlice = pageSlice(filtered.stream().map(this::toPresalePageItem).toList(), pageNum, pageSize);
        fillPageResponse(response, pageNum, pageSlice);
    }

    private void fillHousePage(
            OrderPageResponseVO response,
            Long campId,
            int pageNum,
            int pageSize,
            String orderType,
            String searchContent
    ) {
        String normalizedOrderType = trimToNull(orderType);
        List<OrderQueryRowVO> records = orderQueryMapper.selectHouseOrders(campId, trimToNull(searchContent));
        LocalDate today = LocalDate.now(SHANGHAI_ZONE);
        List<OrderQueryRowVO> filtered = records.stream()
                .filter(row -> matchesOrderType(row, normalizedOrderType, today))
                .toList();
        PageSlice<OrderPageItemVO> pageSlice = pageSlice(filtered.stream().map(this::toPageItem).toList(), pageNum, pageSize);
        fillPageResponse(response, pageNum, pageSlice);
    }

    private void fillLongRentalPage(
            OrderPageResponseVO response,
            Long campId,
            int pageNum,
            int pageSize,
            String orderType,
            String searchContent,
            String keyword,
            String searchCode,
            String dateType,
            String orderStatus,
            Long channelId,
            Long roomCategoryId,
            String liveStatus,
            Long poiId
    ) {
        String normalizedKeyword = firstNonBlank(searchContent, keyword, searchCode);
        List<OrderQueryRowVO> records = orderQueryMapper.selectHouseOrders(campId, normalizedKeyword);
        List<OrderQueryRowVO> filtered = records.stream()
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
        PageSlice<LongRentalOrderPageItemVO> pageSlice = pageSlice(filtered.stream().map(this::toLongRentalPageItem).toList(), pageNum, pageSize);
        fillPageResponse(response, pageNum, pageSlice);
    }

    private <T> void fillPageResponse(OrderPageResponseVO response, int pageNum, PageSlice<T> pageSlice) {
        response.setTotal(pageSlice.total());
        response.setHasNextPage(pageNum < pageSlice.pages());
        response.setPages(pageSlice.pages());
        response.setList(pageSlice.items());
    }

    private <T> PageSlice<T> pageSlice(List<T> items, int pageNum, int pageSize) {
        long total = items.size();
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
        int fromIndex = Math.min((pageNum - 1) * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return new PageSlice<>(total, pages, items.subList(fromIndex, toIndex));
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
            case "7" -> isRefunding(row);
            case "8" -> isExceptionOrder(row);
            default -> true;
        };
    }

    private boolean matchesLongRentalOrderType(OrderQueryRowVO row, String orderType) {
        String normalized = trimToNull(orderType);
        return normalized == null || "11".equals(normalized);
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

    private boolean isRefunding(OrderQueryRowVO row) {
        return hasStatus(row, "refunding");
    }

    private boolean isExceptionOrder(OrderQueryRowVO row) {
        return hasStatus(row, "refunding") || hasStatus(row, "cancelled") || hasStatus(row, "refunded");
    }

    private boolean hasStatus(OrderQueryRowVO row, String expectedStatus) {
        return expectedStatus.equalsIgnoreCase(trimToNull(row.getStatus()));
    }

    private Integer count(List<OrderQueryRowVO> rows, Predicate<OrderQueryRowVO> predicate) {
        return Math.toIntExact(rows.stream().filter(predicate).count());
    }

    private OrderPageItemVO toPageItem(OrderQueryRowVO row) {
        long commissionPrice = defaultLong(row.getCommissionPriceCent());
        long totalRoomPrice = defaultLong(row.getTotalPriceCent());
        long includeCommissionRoomPrice = totalRoomPrice + commissionPrice;
        long otherPrice = 0L;

        OrderDetailViewVO detailView = new OrderDetailViewVO();
        detailView.setPoiName(row.getPoiName());
        detailView.setRoomCategoryName(row.getRoomCategoryName());
        detailView.setRoomCategoryProductName(row.getRoomCategoryProductName());
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

    private HotelPackageOrderPageItemVO toHotelPackagePageItem(OrderQueryRowVO row) {
        int count = resolveHotelPackageCount(row);
        long unitPrice = defaultLong(row.getSellingPriceCent());
        if (unitPrice == 0L && count > 0) {
            unitPrice = defaultLong(row.getTotalPayPriceCent()) / count;
        }
        long paidAmount = defaultLong(row.getTotalPayPriceCent());

        HotelPackageOrderPageItemVO item = new HotelPackageOrderPageItemVO();
        item.setOrderId(row.getOrderId());
        item.setRoomCategoryName(defaultString(row.getRoomCategoryProductName(), defaultString(row.getRoomCategoryName(), "未命名套餐")));
        item.setCount(count);
        item.setUnitPrice(unitPrice);
        item.setSchedulePriceDiff(paidAmount - unitPrice * count);
        item.setPaidAmount(paidAmount);
        item.setContactPhone(defaultString(row.getGuestMobile(), "-"));
        item.setOrderStateName(resolveHotelPackageOrderStateName(row));
        item.setRefundDisplayStateName(resolveHotelPackageRefundDisplayStateName(row));
        item.setOrderChannelName(resolveHotelPackageOrderChannelName(row));
        item.setBookedAt(formatDateTime(row.getCreatedAt()));
        return item;
    }

    private PresaleOrderPageItemVO toPresalePageItem(OrderQueryRowVO row) {
        int count = resolveHotelPackageCount(row);
        long price = defaultLong(row.getSellingPriceCent());
        if (price == 0L && count > 0) {
            price = defaultLong(row.getTotalPriceCent()) / count;
        }

        PresaleOrderDetailViewVO detailView = new PresaleOrderDetailViewVO();
        detailView.setGoodsName(defaultString(row.getRoomCategoryProductName(), "未命名商品"));
        detailView.setRoomCategoryType(row.getRoomCategoryType());
        detailView.setCategoryId(row.getCategoryId());
        detailView.setCategoryName(row.getCategoryName());
        detailView.setCount(count);
        detailView.setPrice(price);

        PresaleOrderPageItemVO item = new PresaleOrderPageItemVO();
        item.setOrderId(row.getOrderId());
        item.setOrderChannelId(row.getChannelId());
        item.setOrderChannelName(defaultString(row.getOrderChannelName(), row.getChannelName()));
        item.setPaymentWayId(row.getPaymentWayId());
        item.setPaymentWayName(row.getPaymentWayName());
        item.setOrderState(resolvePresaleOrderState(row));
        item.setRefundDisplayState(resolvePresaleRefundDisplayState(row));
        item.setTotalAmount(defaultLong(row.getTotalPriceCent()));
        item.setPaidAmount(defaultLong(row.getTotalPayPriceCent()));
        item.setBuyerName(row.getGuestName());
        item.setBuyerMobile(row.getGuestMobile());
        item.setCreatedAt(formatDateTime(row.getCreatedAt()));
        item.setOrderDetailViews(List.of(detailView));
        return item;
    }

    private LongRentalOrderPageItemVO toLongRentalPageItem(OrderQueryRowVO row) {
        long totalRoomPrice = defaultLong(row.getTotalPriceCent());
        long commissionPrice = defaultLong(row.getCommissionPriceCent());

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
        item.setLtOtherPrice(0L);
        item.setLtDepositPrice(0L);
        item.setOrderTotalIncomePrice(totalRoomPrice + commissionPrice);
        item.setLtRentStartDate(toDateString(row.getStartAt()));
        item.setLtRentEndDate(toDateString(row.getEndAt()));
        item.setLtPeriodOfContract(resolveLongRentalContractTerm(row));
        item.setPaymentWayName(row.getPaymentWayName());
        item.setPaymentTime(toDateString(row.getCreatedAt()));
        item.setCreateTimeText(formatDateTime(row.getCreatedAt()));
        item.setIsOccupyStock(isBlank(row.getRoomName()) ? 0 : 1);
        item.setArrangeRoomStatusName(isBlank(row.getRoomName()) ? "未排房" : "已排房");
        item.setIncludeStatisticsName(isLongRentalCancelled(row) ? "不计入统计" : "计入统计");
        item.setContractNo("HT-LR-" + row.getOrderId());
        item.setNextPaymentAmount(0L);
        item.setNextPaymentDate(toDateString(row.getEndAt()));
        return item;
    }

    private Integer resolveOrderState(OrderQueryRowVO row) {
        if (hasStatus(row, "checked_in")) {
            return 4;
        }
        if (hasStatus(row, "completed")) {
            return 2;
        }
        if (hasStatus(row, "cancelled") || hasStatus(row, "refunded")) {
            return 3;
        }
        return 1;
    }

    private Integer resolveDetailDisplayState(OrderQueryRowVO row) {
        if (hasStatus(row, "checked_in")) {
            return 4;
        }
        if (hasStatus(row, "completed")) {
            return 2;
        }
        if (hasStatus(row, "cancelled") || hasStatus(row, "refunded")) {
            return 3;
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

    private Integer resolvePresaleOrderState(OrderQueryRowVO row) {
        if (hasStatus(row, "cancelled") || hasStatus(row, "refunded")) {
            return 5;
        }
        if (hasStatus(row, "completed")) {
            return 4;
        }
        if ("unpaid".equalsIgnoreCase(trimToNull(row.getPaymentStatus()))) {
            return 6;
        }
        if ("paid".equalsIgnoreCase(trimToNull(row.getPaymentStatus()))) {
            return 3;
        }
        return 1;
    }

    private Integer resolvePresaleRefundDisplayState(OrderQueryRowVO row) {
        if (hasStatus(row, "refunding") || "refunding".equalsIgnoreCase(trimToNull(row.getPaymentStatus()))) {
            return 1;
        }
        long refundAmount = defaultLong(row.getRefundPriceCent());
        long paidAmount = defaultLong(row.getTotalPayPriceCent());
        if (refundAmount > 0L && refundAmount < paidAmount) {
            return 2;
        }
        if (refundAmount > 0L
                || hasStatus(row, "refunded")
                || "refunded".equalsIgnoreCase(trimToNull(row.getPaymentStatus()))) {
            return 3;
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

    private String resolveHotelPackageOrderStateCode(OrderQueryRowVO row) {
        if (isHotelPackageCancelled(row)) {
            return "canceled";
        }
        if (hasStatus(row, "completed")) {
            return "finished";
        }
        return "paid";
    }

    private String resolveHotelPackageOrderStateName(OrderQueryRowVO row) {
        return switch (resolveHotelPackageOrderStateCode(row)) {
            case "finished" -> "\u5df2\u5b8c\u6210";
            case "canceled" -> "\u5df2\u53d6\u6d88";
            default -> "\u5df2\u652f\u4ed8";
        };
    }

    private String resolveHotelPackageRefundDisplayStateCode(OrderQueryRowVO row) {
        if (hasStatus(row, "refunding") || "refunding".equalsIgnoreCase(trimToNull(row.getPaymentStatus()))) {
            return "refunding";
        }
        if (defaultLong(row.getRefundPriceCent()) > 0
                || hasStatus(row, "refunded")
                || "refunded".equalsIgnoreCase(trimToNull(row.getPaymentStatus()))) {
            return "refunded";
        }
        return "none";
    }

    private String resolveHotelPackageRefundDisplayStateName(OrderQueryRowVO row) {
        return switch (resolveHotelPackageRefundDisplayStateCode(row)) {
            case "refunding" -> "\u9000\u6b3e\u4e2d";
            case "refunded" -> "\u9000\u6b3e\u6210\u529f";
            default -> "\u65e0\u552e\u540e";
        };
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

    private boolean isHotelPackageCancelled(OrderQueryRowVO row) {
        return hasStatus(row, "cancelled") || hasStatus(row, "refunded");
    }

    private boolean isHotelPackageOrder(OrderQueryRowVO row) {
        return row.getRoomCategoryType() != null && row.getRoomCategoryType() == 4;
    }

    private boolean isPresaleOrderQuery(List<String> roomCategoryTypes) {
        if (roomCategoryTypes == null || roomCategoryTypes.isEmpty()) {
            return false;
        }
        for (String roomCategoryType : roomCategoryTypes) {
            String normalized = trimToNull(roomCategoryType);
            if ("1".equals(normalized) || "2".equals(normalized) || "3".equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPresaleOrder(OrderQueryRowVO row) {
        return row.getRoomCategoryType() != null
                && row.getRoomCategoryType() >= 1
                && row.getRoomCategoryType() <= 3;
    }

    private boolean matchesRoomCategoryTypes(OrderQueryRowVO row, List<String> roomCategoryTypes) {
        if (roomCategoryTypes == null || roomCategoryTypes.isEmpty()) {
            return true;
        }
        if (row.getRoomCategoryType() == null) {
            return false;
        }
        String rowRoomCategoryType = String.valueOf(row.getRoomCategoryType());
        for (String roomCategoryType : roomCategoryTypes) {
            if (rowRoomCategoryType.equals(trimToNull(roomCategoryType))) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesPresaleOrderStates(OrderQueryRowVO row, List<String> orderStates) {
        return matchesRequestedValue(String.valueOf(resolvePresaleOrderState(row)), orderStates);
    }

    private boolean matchesPresaleCategories(OrderQueryRowVO row, List<String> categoryIds) {
        return matchesRequestedValue(trimToNull(row.getCategoryId()), categoryIds);
    }

    private boolean matchesPresaleOrderChannels(OrderQueryRowVO row, List<String> orderChannelIds) {
        return matchesRequestedValue(trimToNull(row.getChannelId()), orderChannelIds);
    }

    private boolean matchesPresalePaymentWays(OrderQueryRowVO row, List<String> paymentWayIds) {
        return matchesRequestedValue(trimToNull(row.getPaymentWayId()), paymentWayIds);
    }

    private boolean matchesPresaleRefundDisplayState(OrderQueryRowVO row, String refundDisplayState) {
        String normalized = trimToNull(refundDisplayState);
        return normalized == null || normalized.equals(String.valueOf(resolvePresaleRefundDisplayState(row)));
    }

    private boolean matchesPresaleBookedRange(OrderQueryRowVO row, Long bookedStartDate, Long bookedEndDate) {
        Long createdAt = toEpochMillis(row.getCreatedAt());
        if (bookedStartDate != null && (createdAt == null || createdAt < bookedStartDate)) {
            return false;
        }
        return bookedEndDate == null || (createdAt != null && createdAt < bookedEndDate);
    }

    private boolean matchesHotelPackageOrderStates(OrderQueryRowVO row, List<String> orderStates) {
        if (orderStates == null || orderStates.isEmpty()) {
            return true;
        }
        String rowState = resolveHotelPackageOrderStateCode(row);
        for (String orderState : orderStates) {
            if (rowState.equals(trimToNull(orderState))) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesRequestedValue(String actualValue, List<String> requestedValues) {
        if (requestedValues == null || requestedValues.isEmpty()) {
            return true;
        }
        String normalizedActual = trimToNull(actualValue);
        if (normalizedActual == null) {
            return false;
        }
        for (String requestedValue : requestedValues) {
            if (normalizedActual.equals(trimToNull(requestedValue))) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesHotelPackageOrderChannels(OrderQueryRowVO row, List<String> orderChannelIds) {
        if (orderChannelIds == null || orderChannelIds.isEmpty()) {
            return true;
        }
        String sourceType = trimToNull(row.getSourceType());
        String channelId = trimToNull(row.getChannelId());
        String channelName = trimToNull(resolveHotelPackageOrderChannelName(row));
        for (String orderChannelId : orderChannelIds) {
            String normalized = trimToNull(orderChannelId);
            if (normalized == null) {
                continue;
            }
            if (normalized.equalsIgnoreCase(defaultString(sourceType, ""))
                    || normalized.equals(channelId)
                    || mapHotelPackageChannelName(normalized).equals(channelName)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesHotelPackagePaymentWays(OrderQueryRowVO row, List<String> paymentWayIds) {
        if (paymentWayIds == null || paymentWayIds.isEmpty()) {
            return true;
        }
        String paymentWayId = trimToNull(row.getPaymentWayId());
        for (String requestPaymentWayId : paymentWayIds) {
            String normalized = trimToNull(requestPaymentWayId);
            if (normalized != null && normalized.equals(paymentWayId)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesHotelPackageRefundDisplayState(OrderQueryRowVO row, String refundDisplayState) {
        String normalized = trimToNull(refundDisplayState);
        return normalized == null || normalized.equals(resolveHotelPackageRefundDisplayStateCode(row));
    }

    private boolean matchesHotelPackageBookedRange(OrderQueryRowVO row, Long bookedStartDate, Long bookedEndDate) {
        Long createdAt = toEpochMillis(row.getCreatedAt());
        if (bookedStartDate != null && (createdAt == null || createdAt < bookedStartDate)) {
            return false;
        }
        return bookedEndDate == null || (createdAt != null && createdAt < bookedEndDate);
    }

    private int resolveHotelPackageCount(OrderQueryRowVO row) {
        return row.getCount() == null || row.getCount() < 1 ? 1 : row.getCount();
    }

    private String resolveHotelPackageOrderChannelName(OrderQueryRowVO row) {
        return defaultString(row.getOrderChannelName(), mapHotelPackageChannelName(row.getSourceType()));
    }

    private String mapHotelPackageChannelName(String sourceType) {
        String normalized = trimToNull(sourceType);
        if (normalized == null) {
            return "\u672a\u77e5\u6765\u6e90";
        }
        return switch (normalized) {
            case "brand" -> "\u54c1\u724c\u5c0f\u7a0b\u5e8f";
            case "wechat" -> "\u5fae\u4fe1\u5546\u57ce";
            case "offline" -> "\u7ebf\u4e0b\u5bfc\u5165";
            case "distribution" -> "\u5206\u9500\u6e20\u9053";
            default -> normalized;
        };
    }

    private boolean containsValue(List<String> values, String expectedValue) {
        if (values == null || expectedValue == null) {
            return false;
        }
        for (String value : values) {
            if (expectedValue.equals(trimToNull(value))) {
                return true;
            }
        }
        return false;
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String defaultString(String value, String fallback) {
        return isBlank(value) ? fallback : value;
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
            throw new BusinessException(40301, "无权访问当前门店订单数据");
        }
        return requestedCampId;
    }

    private record PageSlice<T>(long total, int pages, List<T> items) {
    }
}
