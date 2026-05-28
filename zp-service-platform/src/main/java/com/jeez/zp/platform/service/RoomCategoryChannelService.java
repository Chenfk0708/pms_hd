package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.RoomCategoryChannelOptionsResponseVO;

public interface RoomCategoryChannelService {

    RoomCategoryChannelOptionsResponseVO getRoomCategoryChannels(Long campId, Long userId);
}
