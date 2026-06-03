package com.jeez.zp.order.service;

import com.jeez.zp.order.vo.CouponPageResponseVO;
import com.jeez.zp.order.vo.CouponSendConfigPageResponseVO;

public interface CouponService {

    CouponPageResponseVO getCoupons(Long campId, Long userId, Integer shelfStatus, Integer pageNum, Integer pageSize);

    CouponSendConfigPageResponseVO getCouponSendConfigs(Long campId, Long userId, Integer pageNum, Integer pageSize);
}
