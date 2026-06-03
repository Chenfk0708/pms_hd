package com.jeez.zp.room.controller;

import com.jeez.zp.room.api.CleanLogLegacyResponse;
import com.jeez.zp.room.api.HudsonResponse;
import com.jeez.zp.room.api.TraceIdFactory;
import com.jeez.zp.room.dto.request.CleanLogPageRequest;
import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.security.LoginUserContext;
import com.jeez.zp.room.service.CleanLogService;
import com.jeez.zp.room.vo.CleanLogExportResponseVO;
import com.jeez.zp.room.vo.CleanLogPageDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CleanLogController {

    private static final String DEFAULT_ERROR_MESSAGE = "保洁日志加载失败，请重试";

    private final CleanLogService cleanLogService;

    @PostMapping({"/cleanLog/page/get", "/cleanManage/cleanLog/list"})
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

    @PostMapping("/cleanManage/cleanLog/export")
    public HudsonResponse<CleanLogExportResponseVO> export(@RequestBody CleanLogPageRequest request) {
        return HudsonResponse.success(
                cleanLogService.export(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLong(request.getPoiId()),
                        request.getRoomId(),
                        parseLong(request.getOperatorId()),
                        request.getOperatorStartTime(),
                        request.getOperatorEndTime()
                ),
                TraceIdFactory.next("clean-log-export")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank() || "ALL".equals(value) || "all".equals(value)) {
            return null;
        }
        return Long.valueOf(value);
    }
}
