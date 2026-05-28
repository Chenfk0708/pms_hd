package com.jeez.zp.room.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeez.zp.room.entity.RoomCategoryGroup;
import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.RoomCategoryGroupMapper;
import com.jeez.zp.room.mapper.UserCampMapper;
import com.jeez.zp.room.service.RoomCategoryGroupService;
import com.jeez.zp.room.vo.RoomCategoryGroupVO;
import com.jeez.zp.room.vo.RoomCategoryGroupsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomCategoryGroupServiceImpl implements RoomCategoryGroupService {

    private final RoomCategoryGroupMapper roomCategoryGroupMapper;
    private final UserCampMapper userCampMapper;

    @Override
    public RoomCategoryGroupsResponseVO getRoomCategoryGroups(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<RoomCategoryGroupVO> groups = roomCategoryGroupMapper.selectList(new LambdaQueryWrapper<RoomCategoryGroup>()
                        .eq(RoomCategoryGroup::getCampId, resolvedCampId)
                        .eq(RoomCategoryGroup::getStatus, 1)
                        .eq(RoomCategoryGroup::getIsDeleted, 0)
                        .orderByAsc(RoomCategoryGroup::getSortNo, RoomCategoryGroup::getGroupId))
                .stream()
                .map(this::toVO)
                .toList();

        RoomCategoryGroupsResponseVO response = new RoomCategoryGroupsResponseVO();
        response.setRoomCategoryGroups(groups);
        response.setList(groups);
        return response;
    }

    private RoomCategoryGroupVO toVO(RoomCategoryGroup group) {
        RoomCategoryGroupVO vo = new RoomCategoryGroupVO();
        vo.setRoomCategoryGroupId(String.valueOf(group.getGroupId()));
        vo.setRoomCategoryGroupName(group.getGroupName());
        vo.setGroupId(String.valueOf(group.getGroupId()));
        vo.setGroupName(group.getGroupName());
        vo.setId(String.valueOf(group.getGroupId()));
        vo.setName(group.getGroupName());
        vo.setPoiId(group.getPoiId() == null ? null : String.valueOf(group.getPoiId()));
        vo.setSortNo(group.getSortNo());
        return vo;
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
            throw new BusinessException(40301, "无权访问指定门店");
        }
        return requestedCampId;
    }
}
