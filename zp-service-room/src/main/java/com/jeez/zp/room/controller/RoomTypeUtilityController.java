package com.jeez.zp.room.controller;

import com.jeez.zp.room.api.HudsonResponse;
import com.jeez.zp.room.api.TraceIdFactory;
import com.jeez.zp.room.dto.request.RoomTypeUtilityRequest;
import com.jeez.zp.room.security.LoginUserContext;
import com.jeez.zp.room.service.RoomTypeUtilityService;
import com.jeez.zp.room.vo.RoomTypeFloorResponseVO;
import com.jeez.zp.room.vo.RoomTypeTagResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoomTypeUtilityController {

    private final RoomTypeUtilityService roomTypeUtilityService;

    @PostMapping("/roomType/floor/get")
    public HudsonResponse<RoomTypeFloorResponseVO> getFloors(@RequestBody RoomTypeUtilityRequest request) {
        return HudsonResponse.success(
                roomTypeUtilityService.getFloors(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("room-type-floor-get")
        );
    }

    @PostMapping("/roomType/tag/get")
    public HudsonResponse<RoomTypeTagResponseVO> getTags(@RequestBody RoomTypeUtilityRequest request) {
        return HudsonResponse.success(
                roomTypeUtilityService.getTags(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("room-type-tag-get")
        );
    }
}
