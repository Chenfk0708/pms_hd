package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.DistributionFlowPageResponseVO;
import com.jeez.zp.platform.vo.DistributionOrderPageResponseVO;

public interface DistributionFlowService {

    DistributionFlowPageResponseVO getPage(
            Long campId,
            Long userId,
            Integer pageNum,
            Integer current,
            Integer pageSize,
            String bookingStartDate,
            String bookingEndDate,
            String keyword,
            Boolean breakTemp,
            String settledState
    );

    DistributionOrderPageResponseVO getDistributionOrdersPage(
            Long campId,
            Long userId,
            Integer pageNum,
            Integer current,
            Integer pageSize,
            String bookingStartDate,
            String bookingEndDate,
            String keyword,
            String settledState
    );
}
