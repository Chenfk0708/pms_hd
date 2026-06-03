package com.jeez.zp.room.controller;

import com.jeez.zp.room.api.HudsonResponse;
import com.jeez.zp.room.api.TraceIdFactory;
import com.jeez.zp.room.dto.request.PriceLogPageRequest;
import com.jeez.zp.room.security.LoginUserContext;
import com.jeez.zp.room.service.PriceLogService;
import com.jeez.zp.room.vo.PriceLogExportResponseVO;
import com.jeez.zp.room.vo.PriceLogPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PriceLogController {

    private final PriceLogService priceLogService;

    @PostMapping("/houseManage/logs/price/list")
    public HudsonResponse<PriceLogPageResponseVO> getPage(@RequestBody PriceLogPageRequest request) {
        return HudsonResponse.success(
                priceLogService.getPage(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("price-log-list")
        );
    }

    @PostMapping("/houseManage/logs/price/export")
    public HudsonResponse<PriceLogExportResponseVO> export(@RequestBody PriceLogPageRequest request) {
        return HudsonResponse.success(
                priceLogService.export(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("price-log-export")
        );
    }
}
