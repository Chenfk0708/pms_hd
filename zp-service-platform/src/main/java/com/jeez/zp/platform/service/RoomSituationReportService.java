package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.RoomSituationPageResponseVO;

import java.util.List;

public interface RoomSituationReportService {

    RoomSituationPageResponseVO getDailyRoomStatus(
            Long campId,
            Long userId,
            String date,
            List<Long> poiIds,
            Integer pageNum,
            Integer current,
            Integer pageSize
    );

    RoomSituationPageResponseVO getForwardRoomStatus(
            Long campId,
            Long userId,
            String startDate,
            String endDate,
            List<Long> poiIds,
            Integer pageNum,
            Integer current,
            Integer pageSize
    );
}
