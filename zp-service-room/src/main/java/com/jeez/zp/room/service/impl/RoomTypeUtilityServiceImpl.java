package com.jeez.zp.room.service.impl;

import com.jeez.zp.room.dto.request.RoomTypeUtilityRequest;
import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.RoomTypeOptionRow;
import com.jeez.zp.room.mapper.RoomTypeUtilityMapper;
import com.jeez.zp.room.mapper.RoomTypeUtilityRow;
import com.jeez.zp.room.mapper.UserCampMapper;
import com.jeez.zp.room.service.RoomTypeUtilityService;
import com.jeez.zp.room.vo.RoomTypeFloorResponseVO;
import com.jeez.zp.room.vo.RoomTypeFloorVO;
import com.jeez.zp.room.vo.RoomTypeOptionVO;
import com.jeez.zp.room.vo.RoomTypeTagResponseVO;
import com.jeez.zp.room.vo.RoomTypeTagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomTypeUtilityServiceImpl implements RoomTypeUtilityService {

    private final RoomTypeUtilityMapper roomTypeUtilityMapper;
    private final UserCampMapper userCampMapper;

    @Override
    public RoomTypeFloorResponseVO getFloors(RoomTypeUtilityRequest request, Long userId) {
        QueryScope scope = resolveScope(request, userId);
        List<RoomTypeFloorVO> floors = roomTypeUtilityMapper
                .selectFloors(scope.campId(), scope.poiId(), normalizeKeyword(request.getKeyword()))
                .stream()
                .map(this::toFloorVO)
                .toList();

        RoomTypeFloorResponseVO response = new RoomTypeFloorResponseVO();
        response.setList(floors);
        response.setRows(floors);
        response.setRoomTypeOptions(getRoomTypeOptions(scope));
        return response;
    }

    @Override
    public RoomTypeTagResponseVO getTags(RoomTypeUtilityRequest request, Long userId) {
        QueryScope scope = resolveScope(request, userId);
        List<RoomTypeTagVO> tags = roomTypeUtilityMapper
                .selectTags(scope.campId(), scope.poiId(), normalizeKeyword(request.getKeyword()))
                .stream()
                .map(this::toTagVO)
                .toList();

        RoomTypeTagResponseVO response = new RoomTypeTagResponseVO();
        response.setList(tags);
        response.setRows(tags);
        response.setRoomTypeOptions(getRoomTypeOptions(scope));
        return response;
    }

    private List<RoomTypeOptionVO> getRoomTypeOptions(QueryScope scope) {
        return roomTypeUtilityMapper.selectRoomTypeOptions(scope.campId(), scope.poiId())
                .stream()
                .map(this::toOptionVO)
                .toList();
    }

    private RoomTypeFloorVO toFloorVO(RoomTypeUtilityRow row) {
        RoomTypeFloorVO vo = new RoomTypeFloorVO();
        String id = String.valueOf(row.getId());
        vo.setId(id);
        vo.setFloorId(id);
        vo.setName(row.getName());
        vo.setFloorName(row.getName());
        vo.setPoiId(row.getPoiId() == null ? null : String.valueOf(row.getPoiId()));
        vo.setSortNo(row.getSortNo());
        vo.setRoomTypeCount(nonNullCount(row.getRoomTypeCount()));
        vo.setCount(vo.getRoomTypeCount());
        vo.setRoomTypeIds(splitCsv(row.getRoomTypeIdsText()));
        vo.setRoomTypeNames(splitCsv(row.getRoomTypeNamesText()));
        return vo;
    }

    private RoomTypeTagVO toTagVO(RoomTypeUtilityRow row) {
        RoomTypeTagVO vo = new RoomTypeTagVO();
        String id = String.valueOf(row.getId());
        vo.setId(id);
        vo.setTagId(id);
        vo.setName(row.getName());
        vo.setTagName(row.getName());
        vo.setTagType(row.getTagType());
        vo.setSortNo(row.getSortNo());
        vo.setRoomTypeCount(nonNullCount(row.getRoomTypeCount()));
        vo.setCount(vo.getRoomTypeCount());
        vo.setRoomTypeIds(splitCsv(row.getRoomTypeIdsText()));
        vo.setRoomTypeNames(splitCsv(row.getRoomTypeNamesText()));
        return vo;
    }

    private RoomTypeOptionVO toOptionVO(RoomTypeOptionRow row) {
        RoomTypeOptionVO vo = new RoomTypeOptionVO();
        String id = String.valueOf(row.getId());
        vo.setId(id);
        vo.setRoomCategoryId(id);
        vo.setName(row.getName());
        vo.setLabel(row.getName());
        vo.setPoiId(row.getPoiId() == null ? null : String.valueOf(row.getPoiId()));
        return vo;
    }

    private QueryScope resolveScope(RoomTypeUtilityRequest request, Long userId) {
        Long currentCampId = userCampMapper.selectCurrentCampId(userId);
        if (currentCampId == null) {
            throw new BusinessException(40401, "未找到当前用户门店");
        }
        Long requestedCampId = parseLong(request.getCampId());
        if (requestedCampId != null && !requestedCampId.equals(currentCampId)) {
            throw new BusinessException(40301, "无权访问当前门店房型辅助数据");
        }
        Long poiId = parseLong(firstNotBlank(request.getPoiId(), request.getStoreId()));
        return new QueryScope(requestedCampId == null ? currentCampId : requestedCampId, poiId);
    }

    private static String normalizeKeyword(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }

    private static Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }

    private static String firstNotBlank(String first, String second) {
        return first == null || first.isBlank() ? second : first;
    }

    private static Integer nonNullCount(Integer value) {
        return value == null ? 0 : value;
    }

    private static List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }

    private record QueryScope(Long campId, Long poiId) {
    }
}
