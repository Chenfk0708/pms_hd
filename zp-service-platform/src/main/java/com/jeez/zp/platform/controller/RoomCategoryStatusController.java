package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.RoomCategoryStatusRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.RoomCategoryStatusService;
import com.jeez.zp.platform.vo.RoomCategoryCentralStatusResponseVO;
import com.jeez.zp.platform.vo.RoomCategoryChannelStatusResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoomCategoryStatusController {

    private final RoomCategoryStatusService roomCategoryStatusService;

    @PostMapping("/roomCategoryStatuses/central/get")
    public HudsonResponse<RoomCategoryCentralStatusResponseVO> getCentralStatuses(@RequestBody RoomCategoryStatusRequest request) {
        return HudsonResponse.success(
                roomCategoryStatusService.getCentralStatuses(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getChannelIds(),
                        request.getDate(),
                        request.getDays(),
                        request.getPageNum(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("room-category-statuses-central-get")
        );
    }

    @PostMapping("/roomCategoryStatuses/roomCategory/channel/get")
    public HudsonResponse<RoomCategoryChannelStatusResponseVO> getChannelStatuses(@RequestBody RoomCategoryStatusRequest request) {
        return HudsonResponse.success(
                roomCategoryStatusService.getChannelStatuses(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getChannelIds(),
                        request.getDate(),
                        request.getDays(),
                        request.getPageNum(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("room-category-statuses-room-category-channel-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
