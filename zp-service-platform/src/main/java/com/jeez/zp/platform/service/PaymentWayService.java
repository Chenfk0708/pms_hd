package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.PaymentWaysResponseVO;

public interface PaymentWayService {

    PaymentWaysResponseVO getPaymentWays(Long campId, Long userId);
}
