package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.OrderPageResponseVO;
import com.jeez.zp.platform.vo.OrderReportVO;

import java.util.List;

public interface OrderQueryService {

    OrderReportVO getReport(Long campId, Long userId);

    OrderPageResponseVO getPage(
            Long campId,
            Long userId,
            Integer pageNum,
            Integer pageSize,
            List<String> roomCategoryTypes,
            List<String> orderStates,
            List<String> categoryIds,
            List<String> orderChannelIds,
            List<String> paymentWayIds,
            String refundDisplayState,
            Long bookedStartDate,
            Long bookedEndDate,
            String orderType,
            Integer isLt,
            String searchContent,
            String keyword,
            String searchCode,
            String dateType,
            String orderStatus,
            Long channelId,
            Long roomCategoryId,
            String liveStatus,
            Long poiId
    );
}
