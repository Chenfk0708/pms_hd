package com.jeez.zp.room.service.impl;

import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.RoomStatusesMonthlyMapper;
import com.jeez.zp.room.mapper.UserCampMapper;
import com.jeez.zp.room.service.RoomStatusesMonthlyService;
import com.jeez.zp.room.vo.RoomStatusesMonthlyBlockVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyDailyMonitorVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyInventoryVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyListResponseVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyOccVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyOrderDetailVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyOrderDetailsResponseVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyRedDotVO;
import com.jeez.zp.room.vo.RoomStatusesRoomsPaginationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RoomStatusesMonthlyServiceImpl implements RoomStatusesMonthlyService {

    private static final int DEFAULT_DAYS = 30;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final RoomStatusesMonthlyMapper roomStatusesMonthlyMapper;
    private final UserCampMapper userCampMapper;

    @Override
    public RoomStatusesMonthlyListResponseVO<RoomStatusesMonthlyInventoryVO> getInventory(
            Long campId,
            Long userId,
            String startDate,
            Integer days,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            Long storeId,
            String queryCode
    ) {
        MonthlyQuery query = normalizeQuery(campId, userId, startDate, days, roomCategoryIds, poiIds, storeId, queryCode);
        return listResponse(roomStatusesMonthlyMapper.selectInventoryRows(
                query.campId(), query.startDate(), query.endDate(), query.roomCategoryIds(), query.poiIds(), query.keyword()
        ));
    }

    @Override
    public RoomStatusesMonthlyListResponseVO<RoomStatusesMonthlyDailyMonitorVO> getDailyMonitor(
            Long campId,
            Long userId,
            String startDate,
            Integer days,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            Long storeId,
            String queryCode
    ) {
        MonthlyQuery query = normalizeQuery(campId, userId, startDate, days, roomCategoryIds, poiIds, storeId, queryCode);
        return listResponse(roomStatusesMonthlyMapper.selectDailyMonitorRows(
                query.campId(), query.startDate(), query.endDate(), query.roomCategoryIds(), query.poiIds(), query.keyword()
        ));
    }

    @Override
    public RoomStatusesMonthlyListResponseVO<RoomStatusesMonthlyOccVO> getOcc(
            Long campId,
            Long userId,
            String startDate,
            Integer days,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            Long storeId,
            String queryCode
    ) {
        MonthlyQuery query = normalizeQuery(campId, userId, startDate, days, roomCategoryIds, poiIds, storeId, queryCode);
        return listResponse(roomStatusesMonthlyMapper.selectOccRows(
                query.campId(), query.startDate(), query.endDate(), query.roomCategoryIds(), query.poiIds(), query.keyword()
        ));
    }

    @Override
    public RoomStatusesMonthlyListResponseVO<RoomStatusesMonthlyBlockVO> getBlock(
            Long campId,
            Long userId,
            String startDate,
            Integer days,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            Long storeId,
            String queryCode
    ) {
        normalizeQuery(campId, userId, startDate, days, roomCategoryIds, poiIds, storeId, queryCode);
        return listResponse(List.of());
    }

    @Override
    public RoomStatusesMonthlyListResponseVO<RoomStatusesMonthlyRedDotVO> getRedDot(
            Long campId,
            Long userId,
            String startDate,
            Integer days,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            Long storeId,
            String queryCode
    ) {
        normalizeQuery(campId, userId, startDate, days, roomCategoryIds, poiIds, storeId, queryCode);
        return listResponse(List.of());
    }

    @Override
    public RoomStatusesMonthlyOrderDetailsResponseVO getOrderDetails(
            Long campId,
            Long userId,
            String startDate,
            Integer days,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            Long storeId,
            String queryCode,
            Integer page,
            Integer pageNum,
            Integer current,
            Integer pageSize
    ) {
        MonthlyQuery query = normalizeQuery(campId, userId, startDate, days, roomCategoryIds, poiIds, storeId, queryCode);
        int resolvedPage = normalizePage(page, pageNum, current);
        int resolvedPageSize = normalizePageSize(pageSize);
        long offset = (long) (resolvedPage - 1) * resolvedPageSize;

        long total = roomStatusesMonthlyMapper.countOrderDetails(
                query.campId(), query.startDate(), query.endDate(), query.roomCategoryIds(), query.poiIds(), query.keyword()
        );
        List<RoomStatusesMonthlyOrderDetailVO> rows = total == 0
                ? List.of()
                : roomStatusesMonthlyMapper.selectOrderDetails(
                query.campId(), query.startDate(), query.endDate(), query.roomCategoryIds(), query.poiIds(), query.keyword(), offset, resolvedPageSize
        );

        RoomStatusesMonthlyOrderDetailsResponseVO response = new RoomStatusesMonthlyOrderDetailsResponseVO();
        response.setList(rows);
        response.setOrderArrangementInfos(List.of());
        response.setPagination(toPagination(total, resolvedPage, resolvedPageSize));
        return response;
    }

    private MonthlyQuery normalizeQuery(
            Long campId,
            Long userId,
            String startDate,
            Integer days,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            Long storeId,
            String queryCode
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        LocalDate resolvedStartDate = parseDateOrToday(startDate);
        int resolvedDays = days == null || days < 1 ? DEFAULT_DAYS : days;
        List<Long> normalizedPoiIds = normalizeIds(poiIds);
        if (normalizedPoiIds.isEmpty() && storeId != null) {
            normalizedPoiIds = List.of(storeId);
        }
        return new MonthlyQuery(
                resolvedCampId,
                resolvedStartDate,
                resolvedStartDate.plusDays(resolvedDays),
                normalizeIds(roomCategoryIds),
                normalizedPoiIds,
                trimToNull(queryCode)
        );
    }

    private <T> RoomStatusesMonthlyListResponseVO<T> listResponse(List<T> rows) {
        RoomStatusesMonthlyListResponseVO<T> response = new RoomStatusesMonthlyListResponseVO<>();
        response.setList(rows);
        return response;
    }

    private RoomStatusesRoomsPaginationVO toPagination(long total, int page, int pageSize) {
        RoomStatusesRoomsPaginationVO pagination = new RoomStatusesRoomsPaginationVO();
        pagination.setPage(page);
        pagination.setPageSize(pageSize);
        pagination.setTotal(total);
        return pagination;
    }

    private int normalizePage(Integer page, Integer pageNum, Integer current) {
        Integer candidate = page != null ? page : pageNum;
        if (candidate == null) {
            candidate = current;
        }
        return candidate == null || candidate < 1 ? DEFAULT_PAGE : candidate;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private List<Long> normalizeIds(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private LocalDate parseDateOrToday(String value) {
        if (value == null || value.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            return LocalDate.now();
        }
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
            throw new BusinessException(40401, "未找到当前用户门店");
        }
        if (requestedCampId == null) {
            return currentCampId;
        }
        if (!requestedCampId.equals(currentCampId)) {
            throw new BusinessException(40301, "无权访问当前门店月房态数据");
        }
        return requestedCampId;
    }

    private record MonthlyQuery(
            Long campId,
            LocalDate startDate,
            LocalDate endDate,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            String keyword
    ) {
    }
}