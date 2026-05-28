package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.CampIdRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.RoomCategoryChannelService;
import com.jeez.zp.platform.vo.RoomCategoryChannelOptionsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoomCategoryChannelController {

    private final RoomCategoryChannelService roomCategoryChannelService;

    @PostMapping("/select/calChannel4RoomCategory/get")
    public HudsonResponse<RoomCategoryChannelOptionsResponseVO> getRoomCategoryChannels(
            @RequestBody CampIdRequest request
    ) {
        return HudsonResponse.success(
                roomCategoryChannelService.getRoomCategoryChannels(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId()
                ),
                TraceIdFactory.next("room-category-channels-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
