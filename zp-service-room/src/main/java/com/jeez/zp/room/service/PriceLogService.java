package com.jeez.zp.room.service;

import com.jeez.zp.room.dto.request.PriceLogPageRequest;
import com.jeez.zp.room.vo.PriceLogExportResponseVO;
import com.jeez.zp.room.vo.PriceLogPageResponseVO;

public interface PriceLogService {

    PriceLogPageResponseVO getPage(PriceLogPageRequest request, Long userId);

    PriceLogExportResponseVO export(PriceLogPageRequest request, Long userId);
}
