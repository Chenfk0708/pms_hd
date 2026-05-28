package com.jeez.zp.room.service;

import com.jeez.zp.room.vo.RoomCategoryCentralStatusResponseVO;
import com.jeez.zp.room.vo.RoomCategoryChannelStatusResponseVO;

import java.util.List;

public interface RoomCategoryStatusService {

    RoomCategoryCentralStatusResponseVO getCentralStatuses(
            Long campId,
            Long userId,
            List<String> channelIds,
            String date,
            Integer days,
            Integer pageNum,
            Integer pageSize
    );

    RoomCategoryChannelStatusResponseVO getChannelStatuses(
            Long campId,
            Long userId,
            List<String> channelIds,
            String date,
            Integer days,
            Integer pageNum,
            Integer pageSize
    );
}
