package com.jeez.zp.platform.service;

import com.jeez.zp.platform.dto.request.OtaChannelDetailRequest;
import com.jeez.zp.platform.dto.request.OtaDashboardRequest;
import com.jeez.zp.platform.dto.request.OtaLogPageRequest;
import com.jeez.zp.platform.vo.OtaChannelDetailVO;
import com.jeez.zp.platform.vo.OtaDashboardVO;
import com.jeez.zp.platform.vo.OtaLogPageVO;

public interface OtaService {

    OtaDashboardVO getDashboard(OtaDashboardRequest request, Long userId);

    OtaChannelDetailVO getChannelDetail(OtaChannelDetailRequest request, Long userId);

    OtaLogPageVO getLogPage(OtaLogPageRequest request, Long userId);
}
