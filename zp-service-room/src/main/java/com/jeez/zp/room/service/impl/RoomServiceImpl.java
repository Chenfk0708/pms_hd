package com.jeez.zp.room.service.impl;

import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.RoomMapper;
import com.jeez.zp.room.mapper.UserCampMapper;
import com.jeez.zp.room.service.RoomService;
import com.jeez.zp.room.vo.RoomCategoryRoomsGroupVO;
import com.jeez.zp.room.vo.RoomCategoryRoomsResponseVO;
import com.jeez.zp.room.vo.RoomItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomMapper roomMapper;
    private final UserCampMapper userCampMapper;

    @Override
    public RoomCategoryRoomsResponseVO getRooms(Long campId, Long userId, List<Long> roomCategoryIds, Integer saleType) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<Long> normalizedRoomCategoryIds = normalizeRoomCategoryIds(roomCategoryIds);

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

    private List<Long> normalizeRoomCategoryIds(List<Long> roomCategoryIds) {
        if (roomCategoryIds == null || roomCategoryIds.isEmpty()) {
            return List.of();
        }
        return roomCategoryIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
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
