package com.jeez.zp.room.service;

import com.jeez.zp.room.vo.RoomCategoryCentralStatusResponseVO;
import com.jeez.zp.room.vo.RoomCategoryChannelStatusResponseVO;
import com.jeez.zp.room.vo.RoomCategorySaleStatusSaveResponseVO;

import java.util.List;

public interface RoomCategoryStatusService {

    RoomCategoryCentralStatusResponseVO getCentralStatuses(
            Long campId,
            Long userId,
            List<String> channelIds,
            List<String> roomCategoryIds,
            List<String> poiIds,
            String date,
            Integer days,
            Integer pageNum,
            Integer pageSize
    );

    RoomCategoryChannelStatusResponseVO getChannelStatuses(
            Long campId,
            Long userId,
            List<String> channelIds,
            List<String> roomCategoryIds,
            List<String> poiIds,
            String date,
            Integer days,
            Integer pageNum,
            Integer pageSize
    );
    RoomCategoryCentralStatusResponseVO getRetailStatuses(
            Long campId,
            Long userId,
            List<String> roomCategoryIds,
            List<String> poiIds,
            String date,
            Integer days,
            Integer pageNum,
            Integer pageSize
    );

    RoomCategorySaleStatusSaveResponseVO saveCentralSaleStatus(
            Long campId,
            Long userId,
            String roomCategoryId,
            String date,
            Boolean saleEnabled
    );
}
