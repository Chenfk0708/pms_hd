package com.jeez.zp.room.controller;

import com.jeez.zp.room.api.HudsonResponse;
import com.jeez.zp.room.api.TraceIdFactory;
import com.jeez.zp.room.dto.request.CleanTaskPageRequest;
import com.jeez.zp.room.security.LoginUserContext;
import com.jeez.zp.room.service.CleanTaskService;
import com.jeez.zp.room.vo.CleanTaskDashboardResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CleanTaskController {

    private final CleanTaskService cleanTaskService;

    @PostMapping("/cleanTask/page/get")
    public HudsonResponse<CleanTaskDashboardResponseVO> getPage(@RequestBody CleanTaskPageRequest request) {
        return HudsonResponse.success(
                cleanTaskService.getPage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLong(request.getPoiId()),
                        request.getCleanTime(),
                        parseLong(request.getRoomId()),
                        request.getCleanType(),
                        request.getCleanStatus(),
                        request.getCleanerIds(),
                        request.getPageNum(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("clean-task-page-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
