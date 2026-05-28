package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.RoomStatusOperationLogPageRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.RoomStatusOperationLogService;
import com.jeez.zp.platform.vo.RoomStatusOperationLogPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoomStatusOperationLogController {

    private final RoomStatusOperationLogService roomStatusOperationLogService;

    @PostMapping("/roomStatusOperationLog/page/get/v2")
    public HudsonResponse<RoomStatusOperationLogPageResponseVO> getPage(@RequestBody RoomStatusOperationLogPageRequest request) {
        return HudsonResponse.success(
                roomStatusOperationLogService.getPage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getPageNum(),
                        request.getPageSize(),
                        request.getCurrent(),
                        request.getKeyword(),
                        request.getAdjustType(),
                        request.getChannelId(),
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getCreateStartTime(),
                        request.getCreateEndTime(),
                        request.getUserName()
                ),
                TraceIdFactory.next("room-status-operation-log-page-get-v2")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
