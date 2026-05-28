package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.PaymentMethodVO;
import com.jeez.zp.platform.vo.PaymentSettingListVO;

public interface PaymentSettingService {

    PaymentSettingListVO getPaymentSettings(Long campId, Long userId, Boolean includeDisabled);

    PaymentMethodVO getPaymentSettingDetail(Long methodId, Long userId);
}
