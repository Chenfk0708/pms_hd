package com.jeez.zp.room.service;

import com.jeez.zp.room.vo.RoomCategoryCentralStatusResponseVO;
import com.jeez.zp.room.vo.RoomCategoryChannelStatusResponseVO;
import com.jeez.zp.room.vo.ChannelCalendarPriceSaveResponseVO;
import com.jeez.zp.room.vo.ChannelProductCoefficientSaveResponseVO;
import com.jeez.zp.room.vo.RoomCategorySaleStatusSaveResponseVO;

import java.math.BigDecimal;
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

    ChannelProductCoefficientSaveResponseVO saveChannelProductCoefficient(
            Long campId,
            Long userId,
            String roomCategoryId,
            String channelId,
            String productName,
            String operator,
            BigDecimal coefficientValue
    );

    ChannelProductCoefficientSaveResponseVO saveChannelProductCoefficients(
            Long campId,
            Long userId,
            List<ChannelProductCoefficientInput> items
    );

    ChannelCalendarPriceSaveResponseVO saveChannelCalendarPrices(
            Long campId,
            Long userId,
            boolean overwriteStandalone,
            List<ChannelCalendarPriceInput> items
    );

    record ChannelProductCoefficientInput(
            String roomCategoryId,
            String channelId,
            String productName,
            String operator,
            BigDecimal coefficientValue
    ) {
    }

    record ChannelCalendarPriceInput(
            String roomCategoryId,
            String channelId,
            String productName,
            String date,
            Integer priceUpdateType,
            BigDecimal calendarPrice,
            BigDecimal basePrice
    ) {
    }
}
