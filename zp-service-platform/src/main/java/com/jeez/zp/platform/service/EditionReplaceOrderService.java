package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.EditionReplaceOrderPageResponseVO;

public interface EditionReplaceOrderService {

    EditionReplaceOrderPageResponseVO getReplaceOrders(
            Long campId,
            Long userId,
            Long receiverStartTime,
            Long receiverEndTime,
            Integer pageNum,
            Integer current,
            Integer pageSize
    );
}
