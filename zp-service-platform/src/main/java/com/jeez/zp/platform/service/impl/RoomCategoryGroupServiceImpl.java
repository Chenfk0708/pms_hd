package com.jeez.zp.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeez.zp.platform.entity.RoomCategoryGroup;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.RoomCategoryGroupMapper;
import com.jeez.zp.platform.service.RoomCategoryGroupService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.RoomCategoryGroupVO;
import com.jeez.zp.platform.vo.RoomCategoryGroupsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomCategoryGroupServiceImpl implements RoomCategoryGroupService {

    private final RoomCategoryGroupMapper roomCategoryGroupMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

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
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "褰撳墠鐢ㄦ埛涓婁笅鏂囦笉瀛樺湪");
        }
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!requestedCampId.equals(bundle.getCampId())) {
            throw new BusinessException(40301, "鏃犳潈璁块棶褰撳墠闂ㄥ簵鎴垮瀷鍒嗙粍鏁版嵁");
        }
        return requestedCampId;
    }
}
