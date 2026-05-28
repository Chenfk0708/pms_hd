package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.RoomCategoryPricingMapper;
import com.jeez.zp.platform.service.RoomCategoryStatusService;
import com.jeez.zp.platform.vo.ChannelRoomCategoryStatusVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.PageXVO;
import com.jeez.zp.platform.vo.RoomCategoryCentralStatusResponseVO;
import com.jeez.zp.platform.vo.RoomCategoryChannelStatusResponseVO;
import com.jeez.zp.platform.vo.RoomCategoryChannelStatusRowVO;
import com.jeez.zp.platform.vo.RoomCategoryPricingRowVO;
import com.jeez.zp.platform.vo.RoomCategoryStatusDayVO;
import com.jeez.zp.platform.vo.RoomCategoryStatusRoomVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoomCategoryStatusServiceImpl implements RoomCategoryStatusService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final int DEFAULT_DAYS = 30;
    private static final long DEFAULT_STOCK = 5L;
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RoomCategoryPricingMapper roomCategoryPricingMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public RoomCategoryCentralStatusResponseVO getCentralStatuses(
            Long campId,
            Long userId,
            List<String> channelIds,
            String date,
            Integer days,
            Integer pageNum,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = normalizePageNum(pageNum);
        int resolvedPageSize = normalizePageSize(pageSize);
        LocalDate startDate = resolveStartDate(date);
        int resolvedDays = normalizeDays(days);

        List<RoomCategoryPricingRowVO> rows = roomCategoryPricingMapper.selectRows(
                resolvedCampId,
                null,
                parseLongList(channelIds)
        );

        Map<String, List<RoomCategoryPricingRowVO>> grouped = new LinkedHashMap<>();
        for (RoomCategoryPricingRowVO row : rows) {
            grouped.computeIfAbsent(row.getRoomCategoryId(), ignored -> new ArrayList<>()).add(row);
        }

        List<RoomCategoryStatusRoomVO> roomStatusViews = grouped.values().stream()
                .map(groupRows -> toCentralRoom(groupRows, startDate, resolvedDays))
                .toList();
        PageSlice<RoomCategoryStatusRoomVO> pageSlice = pageSlice(roomStatusViews, resolvedPageNum, resolvedPageSize);

        RoomCategoryCentralStatusResponseVO response = new RoomCategoryCentralStatusResponseVO();
        response.setRoomStatusViews(pageSlice.items());
        response.setPageX(toPageX(pageSlice, resolvedPageNum, resolvedPageSize));
        return response;
    }

    @Override
    public RoomCategoryChannelStatusResponseVO getChannelStatuses(
            Long campId,
            Long userId,
            List<String> channelIds,
            String date,
            Integer days,
            Integer pageNum,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = normalizePageNum(pageNum);
        int resolvedPageSize = normalizePageSize(pageSize);
        LocalDate startDate = resolveStartDate(date);
        int resolvedDays = normalizeDays(days);

        List<RoomCategoryChannelStatusRowVO> rows = roomCategoryPricingMapper.selectRows(
                        resolvedCampId,
                        null,
                        parseLongList(channelIds)
                ).stream()
                .map(row -> toChannelRow(row, startDate, resolvedDays))
                .toList();

        PageSlice<RoomCategoryChannelStatusRowVO> pageSlice = pageSlice(rows, resolvedPageNum, resolvedPageSize);

        RoomCategoryChannelStatusResponseVO response = new RoomCategoryChannelStatusResponseVO();
        response.setList(pageSlice.items());
        response.setPageX(toPageX(pageSlice, resolvedPageNum, resolvedPageSize));
        return response;
    }

    private RoomCategoryStatusRoomVO toCentralRoom(
            List<RoomCategoryPricingRowVO> groupRows,
            LocalDate startDate,
            int days
    ) {
        RoomCategoryPricingRowVO first = groupRows.get(0);
        long basePrice = resolveBasePrice(first);
        List<RoomCategoryStatusDayVO> dayViews = buildDayViews(startDate, days, basePrice, basePrice);

        RoomCategoryStatusRoomVO room = new RoomCategoryStatusRoomVO();
        room.setRoomCategoryId(first.getRoomCategoryId());
        room.setRoomCategoryName(first.getRoomCategoryName());
        room.setNormalPrice(basePrice);
        room.setNormalActualSalePrice(basePrice);
        room.setStatusViews(dayViews);
        room.setChannelRoomCategoryStatuses(groupRows.stream()
                .map(row -> toNestedChannelStatus(row, startDate, days, basePrice))
                .toList());
        return room;
    }

    private ChannelRoomCategoryStatusVO toNestedChannelStatus(
            RoomCategoryPricingRowVO row,
            LocalDate startDate,
            int days,
            long basePrice
    ) {
        ChannelRoomCategoryStatusVO channelStatus = new ChannelRoomCategoryStatusVO();
        channelStatus.setChannelId(row.getChannelId());
        channelStatus.setChannelName(row.getChannelName());
        channelStatus.setChannelRoomCategoryName(resolveProductName(row));
        channelStatus.setExpressValue("1.00");
        channelStatus.setNormalPrice(basePrice);
        channelStatus.setNormalActualSalePrice(basePrice);
        channelStatus.setStatusViews(buildDayViews(startDate, days, basePrice, basePrice));
        return channelStatus;
    }

    private RoomCategoryChannelStatusRowVO toChannelRow(
            RoomCategoryPricingRowVO row,
            LocalDate startDate,
            int days
    ) {
        long basePrice = resolveBasePrice(row);
        RoomCategoryChannelStatusRowVO channelRow = new RoomCategoryChannelStatusRowVO();
        channelRow.setRoomCategoryId(row.getRoomCategoryId());
        channelRow.setRoomCategoryName(row.getRoomCategoryName());
        channelRow.setRoomCategoryProductName(resolveProductName(row));
        channelRow.setChannelId(row.getChannelId());
        channelRow.setChannelName(row.getChannelName());
        channelRow.setExpressValue("1.00");
        channelRow.setNormalPrice(basePrice);
        channelRow.setNormalActualSalePrice(basePrice);
        channelRow.setStatusViews(buildDayViews(startDate, days, basePrice, basePrice));
        return channelRow;
    }

    private List<RoomCategoryStatusDayVO> buildDayViews(LocalDate startDate, int days, long price, long salePrice) {
        List<RoomCategoryStatusDayVO> dayViews = new ArrayList<>();
        for (int index = 0; index < days; index++) {
            LocalDate current = startDate.plusDays(index);
            RoomCategoryStatusDayVO dayView = new RoomCategoryStatusDayVO();
            dayView.setDate(current.format(DATE_FORMATTER));
            dayView.setTotalStock(DEFAULT_STOCK);
            dayView.setPrice(price);
            dayView.setSalePrice(salePrice);
            dayViews.add(dayView);
        }
        return dayViews;
    }

    private String resolveProductName(RoomCategoryPricingRowVO row) {
        if (row.getProductName() != null && !row.getProductName().isBlank()) {
            return row.getProductName();
        }
        return row.getRoomCategoryName() + "<无早>";
    }

    private long resolveBasePrice(RoomCategoryPricingRowVO row) {
        if (row.getNormalActualSalePrice() != null && row.getNormalActualSalePrice() > 0) {
            return row.getNormalActualSalePrice();
        }
        if (row.getNormalPrice() != null && row.getNormalPrice() > 0) {
            return row.getNormalPrice();
        }
        return 0L;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private int normalizeDays(Integer days) {
        return days == null || days < 1 ? DEFAULT_DAYS : days;
    }

    private LocalDate resolveStartDate(String date) {
        if (date == null || date.isBlank()) {
            return LocalDate.now(SHANGHAI_ZONE);
        }
        return LocalDate.parse(date, DATE_FORMATTER);
    }

    private PageXVO toPageX(PageSlice<?> pageSlice, int pageNum, int pageSize) {
        PageXVO pageX = new PageXVO();
        pageX.setTotal(pageSlice.total());
        pageX.setPageNum(pageNum);
        pageX.setPageSize(pageSize);
        pageX.setHasNextPage(pageNum < pageSlice.pages());
        return pageX;
    }

    private List<Long> parseLongList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<Long> parsed = values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(Long::valueOf)
                .toList();
        return parsed.isEmpty() ? null : parsed;
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
            throw new BusinessException(40301, "无权访问当前门店房价数据");
        }
        return requestedCampId;
    }

    private <T> PageSlice<T> pageSlice(List<T> items, int pageNum, int pageSize) {
        long total = items.size();
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
        int fromIndex = Math.min((pageNum - 1) * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return new PageSlice<>(total, pages, items.subList(fromIndex, toIndex));
    }

    private record PageSlice<T>(long total, int pages, List<T> items) {
    }
}
