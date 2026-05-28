package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.RoomCategoryGroupsResponseVO;

public interface RoomCategoryGroupService {

    RoomCategoryGroupsResponseVO getRoomCategoryGroups(Long campId, Long userId);
}
