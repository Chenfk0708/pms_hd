package com.jeez.zp.order.controller;

import com.jeez.zp.order.api.HudsonResponse;
import com.jeez.zp.order.api.TraceIdFactory;
import com.jeez.zp.order.dto.request.CouponPageRequest;
import com.jeez.zp.order.security.LoginUserContext;
import com.jeez.zp.order.service.CouponService;
import com.jeez.zp.order.vo.CouponPageResponseVO;
import com.jeez.zp.order.vo.CouponSendConfigPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/coupons/page/get")
    public HudsonResponse<CouponPageResponseVO> getCoupons(@RequestBody CouponPageRequest request) {
        return HudsonResponse.success(
                couponService.getCoupons(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getShelfStatus(),
                        firstNonNull(request.getPageNum(), request.getCurrent()),
                        request.getPageSize()
                ),
                TraceIdFactory.next("coupons-page-get")
        );
    }

    @PostMapping("/couponSendConfigs/page/get")
    public HudsonResponse<CouponSendConfigPageResponseVO> getCouponSendConfigs(@RequestBody CouponPageRequest request) {
        return HudsonResponse.success(
                couponService.getCouponSendConfigs(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        firstNonNull(request.getPageNum(), request.getCurrent()),
                        request.getPageSize()
                ),
                TraceIdFactory.next("coupon-send-configs-page-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }

    private Integer firstNonNull(Integer first, Integer second) {
        return first != null ? first : second;
    }
}
