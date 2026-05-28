package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.DepositChannelOptionsResponseVO;

public interface DepositChannelService {

    DepositChannelOptionsResponseVO getDepositChannels(Long campId, Long userId);
}
