package com.jeez.zp.room.service.impl;

import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.RoomMapper;
import com.jeez.zp.room.mapper.UserCampMapper;
import com.jeez.zp.room.service.RoomService;
import com.jeez.zp.room.vo.RoomCategoryRoomsGroupVO;
import com.jeez.zp.room.vo.RoomCategoryRoomsResponseVO;
import com.jeez.zp.room.vo.RoomItemVO;
import com.jeez.zp.room.vo.RoomPageItemVO;
import com.jeez.zp.room.vo.RoomPageResponseVO;
import com.jeez.zp.room.vo.RoomStatusesRoomsCategoryVO;
import com.jeez.zp.room.vo.RoomStatusesRoomsPaginationVO;
import com.jeez.zp.room.vo.RoomStatusesRoomsResponseVO;
import com.jeez.zp.room.vo.RoomStatusesRoomsRoomVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int SINGLE_INVENTORY_DISABLED = 0;

    private final RoomMapper roomMapper;
    private final UserCampMapper userCampMapper;

    @Override
    public RoomCategoryRoomsResponseVO getRooms(Long campId, Long userId, List<Long> roomCategoryIds, Integer saleType) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<Long> normalizedRoomCategoryIds = normalizeIds(roomCategoryIds);

        List<RoomCategoryRoomsGroupVO> roomCategoryRooms = roomMapper.selectRoomCategoryGroups(
                resolvedCampId,
                normalizedRoomCategoryIds
        );

        if (roomCategoryRooms.isEmpty()) {
            RoomCategoryRoomsResponseVO response = new RoomCategoryRoomsResponseVO();
            response.setRoomCategoryRooms(List.of());
            return response;
        }

        LinkedHashMap<String, List<RoomItemVO>> roomItemsByCategoryId = roomMapper.selectRooms(
                        resolvedCampId,
                        normalizedRoomCategoryIds,
                        saleType
                ).stream()
                .collect(Collectors.groupingBy(
                        RoomItemVO::getRoomCategoryId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        for (RoomCategoryRoomsGroupVO group : roomCategoryRooms) {
            List<RoomItemVO> rooms = roomItemsByCategoryId.getOrDefault(group.getRoomCategoryId(), List.of());
            for (int index = 0; index < rooms.size(); index++) {
                RoomItemVO room = rooms.get(index);
                room.setSeq(index + 1);
                room.setDeviceViews(List.of());
            }
            group.setRooms(rooms);
        }

        RoomCategoryRoomsResponseVO response = new RoomCategoryRoomsResponseVO();
        response.setRoomCategoryRooms(roomCategoryRooms);
        return response;
    }

    @Override
    public RoomPageResponseVO getRoomsPage(
            Long campId,
            Long userId,
            Long poiId,
            Long storeId,
            List<Long> roomCategoryIds,
            Integer isAvailability,
            Integer saleType,
            String keyword,
            Integer pageNum,
            Integer current,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        Long resolvedPoiId = poiId != null ? poiId : storeId;
        int resolvedPageNum = normalizePageNum(pageNum, current);
        int resolvedPageSize = normalizePageSize(pageSize);
        long offset = (long) (resolvedPageNum - 1) * resolvedPageSize;
        String normalizedKeyword = trimToNull(keyword);
        List<Long> normalizedRoomCategoryIds = normalizeIds(roomCategoryIds);

        long total = roomMapper.countRoomsPage(
                resolvedCampId,
                resolvedPoiId,
                normalizedRoomCategoryIds,
                isAvailability,
                saleType,
                normalizedKeyword
        );

        List<RoomPageItemVO> items = total == 0
                ? List.of()
                : roomMapper.selectRoomsPage(
                resolvedCampId,
                resolvedPoiId,
                normalizedRoomCategoryIds,
                isAvailability,
                saleType,
                normalizedKeyword,
                offset,
                resolvedPageSize
        );

        for (int index = 0; index < items.size(); index++) {
            RoomPageItemVO item = items.get(index);
            item.setSeq((int) offset + index + 1);
        }

        RoomPageResponseVO response = new RoomPageResponseVO();
        response.setTotal(total);
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setPageNum(resolvedPageNum);
        response.setPageSize(resolvedPageSize);
        response.setPages(toPages(total, resolvedPageSize));
        response.setHasNextPage(resolvedPageNum < response.getPages());
        response.setList(items);
        return response;
    }

    @Override
    public RoomStatusesRoomsResponseVO getRoomStatusesRooms(
            Long campId,
            Long userId,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            Long storeId,
            String queryCode,
            Integer page,
            Integer pageNum,
            Integer current,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<Long> normalizedRoomCategoryIds = normalizeIds(roomCategoryIds);
        List<Long> normalizedPoiIds = normalizeIds(poiIds);
        if (normalizedPoiIds.isEmpty() && storeId != null) {
            normalizedPoiIds = List.of(storeId);
        }
        String normalizedKeyword = trimToNull(queryCode);
        int resolvedPage = normalizePage(page, pageNum, current);
        int resolvedPageSize = normalizePageSize(pageSize);
        long offset = (long) (resolvedPage - 1) * resolvedPageSize;

        long total = roomMapper.countRoomStatusesRoomCategories(
                resolvedCampId,
                normalizedRoomCategoryIds,
                normalizedPoiIds,
                normalizedKeyword
        );

        List<RoomStatusesRoomsCategoryVO> categories = total == 0
                ? List.of()
                : roomMapper.selectRoomStatusesRoomCategories(
                resolvedCampId,
                normalizedRoomCategoryIds,
                normalizedPoiIds,
                normalizedKeyword,
                offset,
                resolvedPageSize
        );

        hydrateRoomStatusesRooms(categories, resolvedCampId, normalizedPoiIds, normalizedKeyword);

        RoomStatusesRoomsResponseVO response = new RoomStatusesRoomsResponseVO();
        response.setIsSingleInventory(SINGLE_INVENTORY_DISABLED);
        response.setList(categories);
        response.setPagination(toPagination(total, resolvedPage, resolvedPageSize));
        return response;
    }

    private void hydrateRoomStatusesRooms(
            List<RoomStatusesRoomsCategoryVO> categories,
            Long campId,
            List<Long> poiIds,
            String keyword
    ) {
        if (categories.isEmpty()) {
            return;
        }
        List<Long> roomCategoryIds = categories.stream()
                .map(RoomStatusesRoomsCategoryVO::getRoomCategoryId)
                .map(this::parseLongOrNull)
                .filter(Objects::nonNull)
                .toList();

        LinkedHashMap<String, List<RoomStatusesRoomsRoomVO>> roomsByCategoryId = roomMapper.selectRoomStatusesRooms(
                        campId,
                        roomCategoryIds,
                        poiIds,
                        keyword
                ).stream()
                .collect(Collectors.groupingBy(
                        RoomStatusesRoomsRoomVO::getRoomCategoryId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        for (RoomStatusesRoomsCategoryVO category : categories) {
            category.setRooms(roomsByCategoryId.getOrDefault(category.getRoomCategoryId(), List.of()));
        }
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

    private int normalizePage(Integer page, Integer pageNum, Integer current) {
        Integer candidate = page != null ? page : pageNum;
        if (candidate == null) {
            candidate = current;
        }
        return normalizePageNum(candidate, null);
    }

    private int normalizePageNum(Integer pageNum, Integer current) {
        Integer candidate = pageNum != null ? pageNum : current;
        return candidate == null || candidate < 1 ? DEFAULT_PAGE_NUM : candidate;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private int toPages(long total, int pageSize) {
        return total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
    }

    private RoomStatusesRoomsPaginationVO toPagination(long total, int page, int pageSize) {
        RoomStatusesRoomsPaginationVO pagination = new RoomStatusesRoomsPaginationVO();
        pagination.setPage(page);
        pagination.setPageSize(pageSize);
        pagination.setTotal(total);
        return pagination;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long parseLongOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
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
            throw new BusinessException(40301, "无权访问当前门店房间数据");
        }
        return requestedCampId;
    }
}
