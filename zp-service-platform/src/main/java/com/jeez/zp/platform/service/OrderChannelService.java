package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.OrderChannelOptionsResponseVO;

public interface OrderChannelService {

    OrderChannelOptionsResponseVO getOrderChannels(Long campId, Long userId);
}
