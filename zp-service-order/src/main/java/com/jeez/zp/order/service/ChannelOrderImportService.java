package com.jeez.zp.order.service;

import com.jeez.zp.order.dto.request.ChannelOrderImportRequest;
import com.jeez.zp.order.vo.ChannelOrderImportResponseVO;

public interface ChannelOrderImportService {

    ChannelOrderImportResponseVO importOrder(ChannelOrderImportRequest request, Long userId);
}
