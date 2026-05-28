package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.CleanLogLegacyResponse;
import com.jeez.zp.platform.dto.request.CleanLogPageRequest;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.CleanLogService;
import com.jeez.zp.platform.vo.CleanLogPageDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CleanLogController {

    private static final String DEFAULT_ERROR_MESSAGE = "保洁日志加载失败，请重试";

    private final CleanLogService cleanLogService;

    @PostMapping("/cleanLog/page/get")
    public CleanLogLegacyResponse<CleanLogPageDataVO> getPage(@RequestBody CleanLogPageRequest request) {
        try {
            return CleanLogLegacyResponse.success(
                    cleanLogService.getPage(
                            parseLong(request.getCampId()),
                            LoginUserContext.requiredUserId(),
                            parseLong(request.getPoiId()),
                            request.getRoomId(),
                            parseLong(request.getOperatorId()),
                            request.getOperatorStartTime(),
                            request.getOperatorEndTime(),
                            request.getPageNum(),
                            request.getPageSize()
                    )
            );
        } catch (BusinessException ex) {
            return CleanLogLegacyResponse.failure(ex.getMessage());
        } catch (Exception ex) {
            return CleanLogLegacyResponse.failure(DEFAULT_ERROR_MESSAGE, ex.getMessage());
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
