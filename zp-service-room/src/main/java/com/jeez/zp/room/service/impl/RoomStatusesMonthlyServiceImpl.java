package com.jeez.zp.room.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.jeez.zp.room.dto.request.RoomStatusCloseRequest;
import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.RoomStatusesMonthlyMapper;
import com.jeez.zp.room.mapper.UserCampMapper;
import com.jeez.zp.room.service.RoomStatusesMonthlyService;
import com.jeez.zp.room.vo.RoomStatusCloseResponseVO;
import com.jeez.zp.room.vo.RoomStatusCloseRoomMetaVO;
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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public RoomStatusCloseResponseVO closeRoom(RoomStatusCloseRequest request, Long userId) {
        if (request == null) {
            throw new BusinessException(40001, "request is required");
        }
        Long campId = resolveAccessibleCampId(parseRequiredLong(request.getCampId(), "campId"), userId);
        Long roomCategoryId = parseRequiredLong(request.getRoomCategoryId(), "roomCategoryId");
        Long roomId = parseRequiredLong(request.getRoomId(), "roomId");
        LocalDate bizDate = parseRequiredDate(request.getDate(), "date");
        String reason = defaultString(request.getReason(), "手动关房");

        RoomStatusCloseRoomMetaVO roomMeta = roomStatusesMonthlyMapper.selectCloseRoomMeta(campId, roomCategoryId, roomId);
        if (roomMeta == null) {
            throw new BusinessException(40401, "房间不存在或不可用");
        }
        if (roomStatusesMonthlyMapper.countOrdersOnRoomDate(campId, roomId, bizDate) > 0) {
            throw new BusinessException(40001, "该房间当前日期已有订单，不能关房");
        }

        boolean alreadyClosed = roomStatusesMonthlyMapper.countExistingClosedBlock(campId, roomId, bizDate) > 0;
        roomStatusesMonthlyMapper.upsertClosedBlock(
                IdWorker.getId(),
                campId,
                roomMeta.getPoiId(),
                bizDate,
                roomCategoryId,
                roomId,
                reason,
                userId
        );
        if (!alreadyClosed) {
            int updated = roomStatusesMonthlyMapper.updateDailyCountersForClose(
                    campId, roomMeta.getPoiId(), bizDate, roomCategoryId
            );
            if (updated == 0) {
                roomStatusesMonthlyMapper.insertDailyCountersForClose(
                        IdWorker.getId(), campId, roomMeta.getPoiId(), bizDate, roomCategoryId
                );
            }
        }

        RoomStatusCloseResponseVO response = new RoomStatusCloseResponseVO();
        response.setRoomCategoryId(String.valueOf(roomCategoryId));
        response.setRoomId(String.valueOf(roomId));
        response.setDate(bizDate.toString());
        response.setReason(reason);
        response.setMessage("关房成功");
        return response;
    }

    @Override
    @Transactional
    public RoomStatusCloseResponseVO openRoom(RoomStatusCloseRequest request, Long userId) {
        if (request == null) {
            throw new BusinessException(40001, "request is required");
        }
        Long campId = resolveAccessibleCampId(parseRequiredLong(request.getCampId(), "campId"), userId);
        Long roomCategoryId = parseRequiredLong(request.getRoomCategoryId(), "roomCategoryId");
        Long roomId = parseRequiredLong(request.getRoomId(), "roomId");
        LocalDate bizDate = parseRequiredDate(request.getDate(), "date");

        RoomStatusCloseRoomMetaVO roomMeta = roomStatusesMonthlyMapper.selectCloseRoomMeta(campId, roomCategoryId, roomId);
        if (roomMeta == null) {
            throw new BusinessException(40401, "房间不存在或不可用");
        }

        int opened = roomStatusesMonthlyMapper.openClosedBlock(campId, roomId, bizDate, userId);
        if (opened > 0) {
            roomStatusesMonthlyMapper.updateDailyCountersForOpen(campId, roomMeta.getPoiId(), bizDate, roomCategoryId);
        }

        RoomStatusCloseResponseVO response = new RoomStatusCloseResponseVO();
        response.setRoomCategoryId(String.valueOf(roomCategoryId));
        response.setRoomId(String.valueOf(roomId));
        response.setDate(bizDate.toString());
        response.setMessage("开房成功");
        return response;
    }

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
        MonthlyQuery query = normalizeQuery(campId, userId, startDate, days, roomCategoryIds, poiIds, storeId, queryCode);
        return listResponse(roomStatusesMonthlyMapper.selectBlockRows(
                query.campId(), query.startDate(), query.endDate(), query.roomCategoryIds(), query.poiIds(), query.keyword()
        ));
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

    private Long parseRequiredLong(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(40001, fieldName + " is required");
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            throw new BusinessException(40001, fieldName + " is invalid");
        }
    }

    private LocalDate parseRequiredDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(40001, fieldName + " is required");
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new BusinessException(40001, fieldName + " is invalid");
        }
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
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
