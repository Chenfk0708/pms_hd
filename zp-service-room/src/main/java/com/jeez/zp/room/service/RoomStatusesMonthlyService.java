package com.jeez.zp.room.service;

import com.jeez.zp.room.vo.RoomStatusesMonthlyBlockVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyDailyMonitorVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyInventoryVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyListResponseVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyOccVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyOrderDetailsResponseVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyRedDotVO;

import java.util.List;

public interface RoomStatusesMonthlyService {

    RoomStatusesMonthlyListResponseVO<RoomStatusesMonthlyInventoryVO> getInventory(
            Long campId,
            Long userId,
            String startDate,
            Integer days,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            Long storeId,
            String queryCode
    );

    RoomStatusesMonthlyListResponseVO<RoomStatusesMonthlyDailyMonitorVO> getDailyMonitor(
            Long campId,
            Long userId,
            String startDate,
            Integer days,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            Long storeId,
            String queryCode
    );

    RoomStatusesMonthlyListResponseVO<RoomStatusesMonthlyOccVO> getOcc(
            Long campId,
            Long userId,
            String startDate,
            Integer days,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            Long storeId,
            String queryCode
    );

    RoomStatusesMonthlyListResponseVO<RoomStatusesMonthlyBlockVO> getBlock(
            Long campId,
            Long userId,
            String startDate,
            Integer days,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            Long storeId,
            String queryCode
    );

    RoomStatusesMonthlyListResponseVO<RoomStatusesMonthlyRedDotVO> getRedDot(
            Long campId,
            Long userId,
            String startDate,
            Integer days,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            Long storeId,
            String queryCode
    );

    RoomStatusesMonthlyOrderDetailsResponseVO getOrderDetails(
            Long campId,
            Long userId,
            String startDate,
            Integer days,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            Long storeId,
            String queryCode,
            Integer page,
            Integer pageNum,
            Integer current,
            Integer pageSize
    );
}