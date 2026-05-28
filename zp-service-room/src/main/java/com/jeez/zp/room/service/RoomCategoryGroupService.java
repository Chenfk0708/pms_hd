package com.jeez.zp.room.service;

import com.jeez.zp.room.vo.RoomCategoryGroupsResponseVO;

public interface RoomCategoryGroupService {

    RoomCategoryGroupsResponseVO getRoomCategoryGroups(Long campId, Long userId);
}
