package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.CommodityDetailRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.CommodityService;
import com.jeez.zp.platform.vo.CommodityDetailResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommodityController {

    private final CommodityService commodityService;

    @PostMapping("/youzan/commodity/get")
    public HudsonResponse<CommodityDetailResponseVO> getCommodity(@RequestBody CommodityDetailRequest request) {
        return HudsonResponse.success(
                commodityService.getCommodityDetail(
                        parseLong(request.getCampId()),
                        parseLong(request.getCommodityId()),
                        LoginUserContext.requiredUserId()
                ),
                TraceIdFactory.next("youzan-commodity-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
