package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.RoomMapper;
import com.jeez.zp.platform.service.RoomService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.RoomCategoryRoomsGroupVO;
import com.jeez.zp.platform.vo.RoomCategoryRoomsResponseVO;
import com.jeez.zp.platform.vo.RoomItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomMapper roomMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

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

        Map<String, List<RoomItemVO>> roomItemsByCategoryId = roomMapper.selectRooms(
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
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "当前用户上下文不存在");
        }
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!requestedCampId.equals(bundle.getCampId())) {
            throw new BusinessException(40301, "无权访问当前门店房间数据");
        }
        return requestedCampId;
    }
}
