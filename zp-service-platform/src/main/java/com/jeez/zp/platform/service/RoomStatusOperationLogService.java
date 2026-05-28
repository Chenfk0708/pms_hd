package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.RoomStatusOperationLogPageResponseVO;

public interface RoomStatusOperationLogService {

    RoomStatusOperationLogPageResponseVO getPage(
            Long campId,
            Long userId,
            Integer pageNum,
            Integer pageSize,
            Integer current,
            String keyword,
            Integer adjustType,
            String channelId,
            String startDate,
            String endDate,
            String createStartTime,
            String createEndTime,
            String userName
    );
}
