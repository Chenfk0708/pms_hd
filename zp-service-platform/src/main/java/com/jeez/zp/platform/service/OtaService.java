package com.jeez.zp.platform.service;

import com.jeez.zp.platform.dto.request.OtaChannelDetailRequest;
import com.jeez.zp.platform.dto.request.OtaDashboardRequest;
import com.jeez.zp.platform.vo.OtaChannelDetailVO;
import com.jeez.zp.platform.vo.OtaDashboardVO;

public interface OtaService {

    OtaDashboardVO getDashboard(OtaDashboardRequest request, Long userId);

    OtaChannelDetailVO getChannelDetail(OtaChannelDetailRequest request, Long userId);
}
