package com.jeez.zp.room.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.RoomCategoryPricingMapper;
import com.jeez.zp.room.mapper.RoomStatusesMonthlyMapper;
import com.jeez.zp.room.mapper.UserCampMapper;
import com.jeez.zp.room.service.RoomCategoryStatusService;
import com.jeez.zp.room.vo.ChannelRoomCategoryStatusVO;
import com.jeez.zp.room.vo.PageXVO;
import com.jeez.zp.room.vo.RoomCategoryAvailableStockRowVO;
import com.jeez.zp.room.vo.RoomCategoryCentralStatusResponseVO;
import com.jeez.zp.room.vo.RoomCategoryChannelStatusResponseVO;
import com.jeez.zp.room.vo.RoomCategoryChannelStatusRowVO;
import com.jeez.zp.room.vo.RoomCategoryPriceSnapshotRowVO;
import com.jeez.zp.room.vo.RoomCategoryPricingRowVO;
import com.jeez.zp.room.vo.RoomCategorySaleStatusSaveResponseVO;
import com.jeez.zp.room.vo.RoomCategoryStatusDayVO;
import com.jeez.zp.room.vo.RoomCategoryStatusRoomVO;
import com.jeez.zp.room.vo.RoomStatusCloseRoomMetaVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private static final String CENTRAL_PRICE_TYPE_ACTIVE = "active";
    private static final String CENTRAL_PRICE_TYPE_DISABLED = "disabled";
    private static final String CENTRAL_SALE_STATUS_DISABLED_REASON = "\u4e2d\u592e\u4ef7\u505c\u552e\u8054\u52a8\u5173\u623f";
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RoomCategoryPricingMapper roomCategoryPricingMapper;
    private final RoomStatusesMonthlyMapper roomStatusesMonthlyMapper;
    private final UserCampMapper userCampMapper;

    @Override
    public RoomCategoryCentralStatusResponseVO getCentralStatuses(
            Long campId,
            Long userId,
            List<String> channelIds,
            List<String> roomCategoryIds,
            List<String> poiIds,
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
        List<Long> normalizedRoomCategoryIds = parseLongList(roomCategoryIds);
        List<Long> normalizedChannelIds = parseLongList(channelIds);
        List<Long> normalizedPoiIds = parseLongList(poiIds);

        List<RoomCategoryPricingRowVO> rows = roomCategoryPricingMapper.selectRows(
                resolvedCampId,
                normalizedRoomCategoryIds,
                normalizedChannelIds,
                normalizedPoiIds
        );

        Map<String, List<RoomCategoryPricingRowVO>> grouped = new LinkedHashMap<>();
        for (RoomCategoryPricingRowVO row : rows) {
            grouped.computeIfAbsent(row.getRoomCategoryId(), ignored -> new ArrayList<>()).add(row);
        }
        List<Long> groupedRoomCategoryIds = grouped.keySet().stream().map(Long::valueOf).toList();
        Map<String, Map<String, RoomCategoryPriceSnapshotRowVO>> snapshots = selectCentralSnapshots(
                resolvedCampId,
                groupedRoomCategoryIds,
                startDate,
                resolvedDays
        );
        Map<String, Map<String, Long>> stocks = selectAvailableStocks(
                resolvedCampId,
                groupedRoomCategoryIds,
                normalizedPoiIds,
                startDate,
                resolvedDays
        );

        List<RoomCategoryStatusRoomVO> roomStatusViews = grouped.values().stream()
                .map(groupRows -> {
                    String roomCategoryId = groupRows.get(0).getRoomCategoryId();
                    return toCentralRoom(
                            groupRows,
                            startDate,
                            resolvedDays,
                            snapshots.getOrDefault(roomCategoryId, Map.of()),
                            stocks.getOrDefault(roomCategoryId, Map.of())
                    );
                })
                .toList();
        PageSlice<RoomCategoryStatusRoomVO> pageSlice = pageSlice(roomStatusViews, resolvedPageNum, resolvedPageSize);

        RoomCategoryCentralStatusResponseVO response = new RoomCategoryCentralStatusResponseVO();
        response.setRoomStatusViews(pageSlice.items());
        response.setPageX(toPageX(pageSlice, resolvedPageNum, resolvedPageSize));
        return response;
    }

    @Override
    @Transactional
    public RoomCategorySaleStatusSaveResponseVO saveCentralSaleStatus(
            Long campId,
            Long userId,
            String roomCategoryId,
            String date,
            Boolean saleEnabled
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        Long resolvedRoomCategoryId = parseRequiredLong(roomCategoryId, "roomCategoryId");
        LocalDate resolvedDate = resolveStartDate(date);
        boolean nextSaleEnabled = Boolean.TRUE.equals(saleEnabled);
        String snapshotStatus = nextSaleEnabled ? CENTRAL_PRICE_TYPE_ACTIVE : CENTRAL_PRICE_TYPE_DISABLED;

        int rows = roomCategoryPricingMapper.upsertCentralSaleStatus(
                IdWorker.getId(),
                resolvedCampId,
                resolvedRoomCategoryId,
                resolvedDate,
                snapshotStatus
        );
        if (rows == 0) {
            throw new BusinessException(40404, "未找到当前房型，无法更新售卖状态");
        }

        if (nextSaleEnabled) {
            openCentralSaleStatusClosedRooms(resolvedCampId, resolvedRoomCategoryId, resolvedDate, userId);
        } else {
            closeCentralSaleStatusAvailableRooms(resolvedCampId, resolvedRoomCategoryId, resolvedDate, userId);
        }

        RoomCategorySaleStatusSaveResponseVO response = new RoomCategorySaleStatusSaveResponseVO();
        response.setRoomCategoryId(String.valueOf(resolvedRoomCategoryId));
        response.setDate(resolvedDate.format(DATE_FORMATTER));
        response.setSaleEnabled(nextSaleEnabled);
        return response;
    }

    private void closeCentralSaleStatusAvailableRooms(
            Long campId,
            Long roomCategoryId,
            LocalDate bizDate,
            Long userId
    ) {
        List<RoomStatusCloseRoomMetaVO> roomMetas = roomStatusesMonthlyMapper.selectCentralSaleStatusCloseRoomMetas(
                campId,
                roomCategoryId,
                bizDate
        );
        for (RoomStatusCloseRoomMetaVO roomMeta : roomMetas) {
            int affected = roomStatusesMonthlyMapper.upsertCentralSaleStatusClosedBlock(
                    IdWorker.getId(),
                    campId,
                    roomMeta.getPoiId(),
                    bizDate,
                    roomCategoryId,
                    roomMeta.getRoomId(),
                    CENTRAL_SALE_STATUS_DISABLED_REASON,
                    userId
            );
            if (affected > 0) {
                decrementDailyCounters(campId, roomMeta.getPoiId(), bizDate, roomCategoryId);
            }
        }
    }

    private void openCentralSaleStatusClosedRooms(
            Long campId,
            Long roomCategoryId,
            LocalDate bizDate,
            Long userId
    ) {
        List<RoomStatusCloseRoomMetaVO> roomMetas = roomStatusesMonthlyMapper.selectCentralSaleStatusClosedBlockMetas(
                campId,
                roomCategoryId,
                bizDate,
                CENTRAL_SALE_STATUS_DISABLED_REASON
        );
        for (RoomStatusCloseRoomMetaVO roomMeta : roomMetas) {
            int affected = roomStatusesMonthlyMapper.openCentralSaleStatusClosedBlock(
                    campId,
                    roomMeta.getRoomId(),
                    bizDate,
                    CENTRAL_SALE_STATUS_DISABLED_REASON,
                    userId
            );
            if (affected > 0) {
                roomStatusesMonthlyMapper.updateDailyCountersForOpen(
                        campId,
                        roomMeta.getPoiId(),
                        bizDate,
                        roomCategoryId
                );
            }
        }
    }

    private void decrementDailyCounters(Long campId, Long poiId, LocalDate bizDate, Long roomCategoryId) {
        int updated = roomStatusesMonthlyMapper.updateDailyCountersForClose(
                campId,
                poiId,
                bizDate,
                roomCategoryId
        );
        if (updated == 0) {
            roomStatusesMonthlyMapper.insertDailyCountersForClose(
                    IdWorker.getId(),
                    campId,
                    poiId,
                    bizDate,
                    roomCategoryId
            );
        }
    }

    @Override
    public RoomCategoryChannelStatusResponseVO getChannelStatuses(
            Long campId,
            Long userId,
            List<String> channelIds,
            List<String> roomCategoryIds,
            List<String> poiIds,
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
        List<Long> normalizedRoomCategoryIds = parseLongList(roomCategoryIds);
        List<Long> normalizedChannelIds = parseLongList(channelIds);
        List<Long> normalizedPoiIds = parseLongList(poiIds);

        List<RoomCategoryPricingRowVO> pricingRows = roomCategoryPricingMapper.selectRows(
                resolvedCampId,
                normalizedRoomCategoryIds,
                normalizedChannelIds,
                normalizedPoiIds
        );
        List<Long> pricedRoomCategoryIds = pricingRows.stream()
                .map(RoomCategoryPricingRowVO::getRoomCategoryId)
                .distinct()
                .map(Long::valueOf)
                .toList();
        Map<String, Map<String, Long>> stocks = selectAvailableStocks(
                resolvedCampId,
                pricedRoomCategoryIds,
                normalizedPoiIds,
                startDate,
                resolvedDays
        );

        List<RoomCategoryChannelStatusRowVO> rows = pricingRows.stream()
                .filter(this::hasChannel)
                .map(row -> toChannelRow(
                        row,
                        startDate,
                        resolvedDays,
                        stocks.getOrDefault(row.getRoomCategoryId(), Map.of())
                ))
                .toList();

        PageSlice<RoomCategoryChannelStatusRowVO> pageSlice = pageSlice(rows, resolvedPageNum, resolvedPageSize);

        RoomCategoryChannelStatusResponseVO response = new RoomCategoryChannelStatusResponseVO();
        response.setList(pageSlice.items());
        response.setPageX(toPageX(pageSlice, resolvedPageNum, resolvedPageSize));
        return response;
    }


    @Override
    public RoomCategoryCentralStatusResponseVO getRetailStatuses(
            Long campId,
            Long userId,
            List<String> roomCategoryIds,
            List<String> poiIds,
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
        List<Long> normalizedRoomCategoryIds = parseLongList(roomCategoryIds);
        List<Long> normalizedPoiIds = parseLongList(poiIds);

        List<RoomCategoryPricingRowVO> retailRows = roomCategoryPricingMapper.selectRetailRows(
                resolvedCampId,
                normalizedRoomCategoryIds,
                normalizedPoiIds
        );
        List<Long> retailRoomCategoryIds = retailRows.stream()
                .map(RoomCategoryPricingRowVO::getRoomCategoryId)
                .distinct()
                .map(Long::valueOf)
                .toList();
        Map<String, Map<String, Long>> stocks = selectAvailableStocks(
                resolvedCampId,
                retailRoomCategoryIds,
                normalizedPoiIds,
                startDate,
                resolvedDays
        );

        List<RoomCategoryStatusRoomVO> roomStatusViews = retailRows.stream()
                .map(row -> toRetailRoom(
                        row,
                        startDate,
                        resolvedDays,
                        stocks.getOrDefault(row.getRoomCategoryId(), Map.of())
                ))
                .toList();
        PageSlice<RoomCategoryStatusRoomVO> pageSlice = pageSlice(roomStatusViews, resolvedPageNum, resolvedPageSize);

        RoomCategoryCentralStatusResponseVO response = new RoomCategoryCentralStatusResponseVO();
        response.setRoomStatusViews(pageSlice.items());
        response.setPageX(toPageX(pageSlice, resolvedPageNum, resolvedPageSize));
        return response;
    }

    private RoomCategoryStatusRoomVO toRetailRoom(
            RoomCategoryPricingRowVO row,
            LocalDate startDate,
            int days,
            Map<String, Long> stocks
    ) {
        long basePrice = resolveBasePrice(row);
        RoomCategoryStatusRoomVO room = new RoomCategoryStatusRoomVO();
        room.setRoomCategoryId(row.getRoomCategoryId());
        room.setRoomCategoryName(row.getRoomCategoryName());
        room.setNormalPrice(basePrice);
        room.setNormalActualSalePrice(basePrice);
        room.setStatusViews(buildRetailDayViews(row, startDate, days, basePrice, stocks));
        room.setChannelRoomCategoryStatuses(List.of());
        return room;
    }

    private RoomCategoryStatusRoomVO toCentralRoom(
            List<RoomCategoryPricingRowVO> groupRows,
            LocalDate startDate,
            int days,
            Map<String, RoomCategoryPriceSnapshotRowVO> snapshots,
            Map<String, Long> stocks
    ) {
        RoomCategoryPricingRowVO first = groupRows.get(0);
        long basePrice = resolveBasePrice(first);
        List<RoomCategoryStatusDayVO> dayViews = buildCentralDayViews(startDate, days, basePrice, snapshots, stocks);

        RoomCategoryStatusRoomVO room = new RoomCategoryStatusRoomVO();
        room.setRoomCategoryId(first.getRoomCategoryId());
        room.setRoomCategoryName(first.getRoomCategoryName());
        room.setNormalPrice(basePrice);
        room.setNormalActualSalePrice(basePrice);
        room.setStatusViews(dayViews);
        room.setChannelRoomCategoryStatuses(groupRows.stream()
                .filter(this::hasChannel)
                .map(row -> toNestedChannelStatus(row, startDate, days, basePrice, stocks))
                .toList());
        return room;
    }

    private Map<String, Map<String, RoomCategoryPriceSnapshotRowVO>> selectCentralSnapshots(
            Long campId,
            List<Long> roomCategoryIds,
            LocalDate startDate,
            int days
    ) {
        if (roomCategoryIds.isEmpty()) {
            return Map.of();
        }
        LocalDate endDate = startDate.plusDays(days - 1L);
        Map<String, Map<String, RoomCategoryPriceSnapshotRowVO>> snapshots = new LinkedHashMap<>();
        for (RoomCategoryPriceSnapshotRowVO row : roomCategoryPricingMapper.selectCentralPriceSnapshots(campId, roomCategoryIds, startDate, endDate)) {
            snapshots.computeIfAbsent(row.getRoomCategoryId(), ignored -> new LinkedHashMap<>())
                    .putIfAbsent(row.getBizDate(), row);
        }
        return snapshots;
    }

    private Map<String, Map<String, Long>> selectAvailableStocks(
            Long campId,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            LocalDate startDate,
            int days
    ) {
        if (roomCategoryIds.isEmpty()) {
            return Map.of();
        }
        LocalDate endDate = startDate.plusDays(days - 1L);
        Map<String, Map<String, Long>> stocks = new LinkedHashMap<>();
        for (RoomCategoryAvailableStockRowVO row : roomCategoryPricingMapper.selectAvailableStockRows(
                campId,
                roomCategoryIds,
                poiIds,
                startDate,
                endDate
        )) {
            stocks.computeIfAbsent(row.getRoomCategoryId(), ignored -> new LinkedHashMap<>())
                    .put(row.getDate(), row.getTotalStock() == null ? 0L : row.getTotalStock());
        }
        return stocks;
    }

    private ChannelRoomCategoryStatusVO toNestedChannelStatus(
            RoomCategoryPricingRowVO row,
            LocalDate startDate,
            int days,
            long basePrice,
            Map<String, Long> stocks
    ) {
        ChannelRoomCategoryStatusVO channelStatus = new ChannelRoomCategoryStatusVO();
        channelStatus.setChannelId(row.getChannelId());
        channelStatus.setChannelName(row.getChannelName());
        channelStatus.setChannelRoomCategoryName(resolveProductName(row));
        channelStatus.setExpressValue("1.00");
        channelStatus.setNormalPrice(basePrice);
        channelStatus.setNormalActualSalePrice(basePrice);
        channelStatus.setStatusViews(buildDayViews(startDate, days, basePrice, basePrice, stocks));
        return channelStatus;
    }

    private RoomCategoryChannelStatusRowVO toChannelRow(
            RoomCategoryPricingRowVO row,
            LocalDate startDate,
            int days,
            Map<String, Long> stocks
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
        channelRow.setStatusViews(buildDayViews(startDate, days, basePrice, basePrice, stocks));
        return channelRow;
    }

    private boolean hasChannel(RoomCategoryPricingRowVO row) {
        return row.getChannelId() != null && !row.getChannelId().isBlank();
    }


    private List<RoomCategoryStatusDayVO> buildRetailDayViews(
            RoomCategoryPricingRowVO row,
            LocalDate startDate,
            int days,
            long fallbackPrice,
            Map<String, Long> stocks
    ) {
        Map<String, Long> pricesByDate = parseStatusPrices(row.getStatusPricesText());
        List<RoomCategoryStatusDayVO> dayViews = new ArrayList<>();
        for (int index = 0; index < days; index++) {
            LocalDate current = startDate.plusDays(index);
            String dateValue = current.format(DATE_FORMATTER);
            long price = pricesByDate.getOrDefault(dateValue, fallbackPrice);
            RoomCategoryStatusDayVO dayView = new RoomCategoryStatusDayVO();
            dayView.setDate(dateValue);
            dayView.setTotalStock(stockForDate(stocks, dateValue));
            dayView.setPrice(price);
            dayView.setSalePrice(price);
            dayView.setSaleEnabled(true);
            dayViews.add(dayView);
        }
        return dayViews;
    }

    private Map<String, Long> parseStatusPrices(String statusPricesText) {
        Map<String, Long> pricesByDate = new LinkedHashMap<>();
        if (statusPricesText == null || statusPricesText.isBlank()) {
            return pricesByDate;
        }
        for (String item : statusPricesText.split(",")) {
            String[] parts = item.split(":", 2);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                continue;
            }
            pricesByDate.put(parts[0], Long.valueOf(parts[1]));
        }
        return pricesByDate;
    }

    private List<RoomCategoryStatusDayVO> buildDayViews(
            LocalDate startDate,
            int days,
            long price,
            long salePrice,
            Map<String, Long> stocks
    ) {
        List<RoomCategoryStatusDayVO> dayViews = new ArrayList<>();
        for (int index = 0; index < days; index++) {
            LocalDate current = startDate.plusDays(index);
            String dateValue = current.format(DATE_FORMATTER);
            RoomCategoryStatusDayVO dayView = new RoomCategoryStatusDayVO();
            dayView.setDate(dateValue);
            dayView.setTotalStock(stockForDate(stocks, dateValue));
            dayView.setPrice(price);
            dayView.setSalePrice(salePrice);
            dayView.setSaleEnabled(true);
            dayViews.add(dayView);
        }
        return dayViews;
    }

    private List<RoomCategoryStatusDayVO> buildCentralDayViews(
            LocalDate startDate,
            int days,
            long fallbackPrice,
            Map<String, RoomCategoryPriceSnapshotRowVO> snapshots,
            Map<String, Long> stocks
    ) {
        List<RoomCategoryStatusDayVO> dayViews = new ArrayList<>();
        for (int index = 0; index < days; index++) {
            LocalDate current = startDate.plusDays(index);
            String dateValue = current.format(DATE_FORMATTER);
            RoomCategoryPriceSnapshotRowVO snapshot = snapshots.get(dateValue);
            long price = snapshot != null && snapshot.getPriceCent() != null ? snapshot.getPriceCent() : fallbackPrice;

            RoomCategoryStatusDayVO dayView = new RoomCategoryStatusDayVO();
            dayView.setDate(dateValue);
            dayView.setTotalStock(stockForDate(stocks, dateValue));
            dayView.setPrice(price);
            dayView.setSalePrice(price);
            dayView.setSaleEnabled(snapshot == null || CENTRAL_PRICE_TYPE_ACTIVE.equalsIgnoreCase(snapshot.getStatus()));
            dayViews.add(dayView);
        }
        return dayViews;
    }

    private long stockForDate(Map<String, Long> stocks, String date) {
        return stocks.getOrDefault(date, 0L);
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

    private Long parseRequiredLong(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(40001, fieldName + "不能为空");
        }
        return Long.valueOf(value);
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        Long currentCampId = userCampMapper.selectCurrentCampId(userId);
        if (currentCampId == null) {
            throw new BusinessException(40401, "鏈壘鍒板綋鍓嶇敤鎴烽棬搴?");
        }
        if (requestedCampId == null) {
            return currentCampId;
        }
        if (!requestedCampId.equals(currentCampId)) {
            throw new BusinessException(40301, "鏃犳潈璁块棶褰撳墠闂ㄥ簵鎴夸环鏁版嵁");
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
