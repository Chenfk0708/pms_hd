package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.CommodityDetailResponseVO;

public interface CommodityService {

    CommodityDetailResponseVO getCommodityDetail(Long campId, Long commodityId, Long userId);
}
