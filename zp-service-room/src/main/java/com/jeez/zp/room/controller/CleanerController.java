package com.jeez.zp.room.controller;

import com.jeez.zp.room.api.HudsonResponse;
import com.jeez.zp.room.api.TraceIdFactory;
import com.jeez.zp.room.dto.request.CleanerListRequest;
import com.jeez.zp.room.dto.request.CleanerPageRequest;
import com.jeez.zp.room.dto.request.CleanerSaveRequest;
import com.jeez.zp.room.security.LoginUserContext;
import com.jeez.zp.room.service.CleanerService;
import com.jeez.zp.room.vo.CleanerExportResponseVO;
import com.jeez.zp.room.vo.CleanerListItemVO;
import com.jeez.zp.room.vo.CleanerPageResponseVO;
import com.jeez.zp.room.vo.CleanerSaveResponseVO;
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

    @PostMapping("/cleaner/page/get")
    public HudsonResponse<CleanerPageResponseVO> getPage(@RequestBody CleanerPageRequest request) {
        return HudsonResponse.success(
                cleanerService.getPage(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("cleaner-page-get")
        );
    }

    @PostMapping("/cleaner/save")
    public HudsonResponse<CleanerSaveResponseVO> save(@RequestBody CleanerSaveRequest request) {
        return HudsonResponse.success(
                cleanerService.save(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("cleaner-save")
        );
    }

    @PostMapping("/cleaner/export")
    public HudsonResponse<CleanerExportResponseVO> export(@RequestBody CleanerPageRequest request) {
        return HudsonResponse.success(
                cleanerService.export(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("cleaner-export")
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
