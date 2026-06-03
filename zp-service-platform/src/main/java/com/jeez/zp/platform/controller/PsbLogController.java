package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.CleanLogLegacyResponse;
import com.jeez.zp.platform.dto.request.PsbLogPageRequest;
import com.jeez.zp.platform.dto.request.PsbLogRetryRequest;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.PsbLogService;
import com.jeez.zp.platform.vo.PsbLogPageDataVO;
import com.jeez.zp.platform.vo.PsbLogRowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PsbLogController {

    private static final String DEFAULT_ERROR_MESSAGE = "公安上报日志加载失败，请重试";

    private final PsbLogService psbLogService;

    @PostMapping("/checkinGuestPsbLog/page/get")
    public CleanLogLegacyResponse<PsbLogPageDataVO> getPage(@RequestBody PsbLogPageRequest request) {
        try {
            return CleanLogLegacyResponse.success(
                    psbLogService.getPage(
                            parseLong(request.getCampId()),
                            LoginUserContext.requiredUserId(),
                            parseLong(request.getPoiId()),
                            request.getKeyword(),
                            request.getBizType(),
                            request.getState(),
                            request.getPageNum() != null ? request.getPageNum() : request.getCurrent(),
                            request.getPageSize()
                    )
            );
        } catch (BusinessException ex) {
            return CleanLogLegacyResponse.failure(ex.getMessage());
        } catch (Exception ex) {
            return CleanLogLegacyResponse.failure(DEFAULT_ERROR_MESSAGE, ex.getMessage());
        }
    }

    @PostMapping("/checkinGuestPsbLog/retry")
    public CleanLogLegacyResponse<PsbLogRowVO> retry(@RequestBody PsbLogRetryRequest request) {
        try {
            return CleanLogLegacyResponse.success(
                    psbLogService.retry(
                            parseLong(request.getCampId()),
                            LoginUserContext.requiredUserId(),
                            parseLong(request.getId()),
                            request.getOrderNo()
                    )
            );
        } catch (BusinessException ex) {
            return CleanLogLegacyResponse.failure(ex.getMessage());
        } catch (Exception ex) {
            return CleanLogLegacyResponse.failure("公安上报重试失败，请重试", ex.getMessage());
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
