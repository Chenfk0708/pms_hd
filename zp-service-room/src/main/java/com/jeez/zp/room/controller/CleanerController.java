package com.jeez.zp.room.controller;

import com.jeez.zp.room.api.HudsonResponse;
import com.jeez.zp.room.api.TraceIdFactory;
import com.jeez.zp.room.dto.request.CleanerListRequest;
import com.jeez.zp.room.security.LoginUserContext;
import com.jeez.zp.room.service.CleanerService;
import com.jeez.zp.room.vo.CleanerListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CleanerController {

    private final CleanerService cleanerService;

    @PostMapping("/cleaner/list/get")
    public HudsonResponse<List<CleanerListItemVO>> getCleaners(@RequestBody CleanerListRequest request) {
        return HudsonResponse.success(
                cleanerService.getCleaners(
                        parseLongOrNull(request.getCampId()),
                        LoginUserContext.requiredUserId()
                ),
                TraceIdFactory.next("cleaner-list-get")
        );
    }

    private Long parseLongOrNull(String value) {
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
