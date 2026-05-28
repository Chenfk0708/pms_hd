package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.PaymentTypeGroupsResponseVO;
import com.jeez.zp.platform.vo.PaymentTypesResponseVO;

import java.util.List;

public interface PaymentTypeService {

    PaymentTypesResponseVO getPaymentTypes(Long campId, Long userId);

    PaymentTypeGroupsResponseVO getPaymentTypesV2(Long campId, Long userId, List<Integer> bizTypes, Integer isEnable);
}
