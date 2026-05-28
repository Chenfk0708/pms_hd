package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.CampIdRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.RoomCategoryGroupService;
import com.jeez.zp.platform.vo.RoomCategoryGroupsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoomCategoryGroupController {

    private final RoomCategoryGroupService roomCategoryGroupService;

    @PostMapping("/roomCategoryGroups/get")
    public HudsonResponse<RoomCategoryGroupsResponseVO> getRoomCategoryGroups(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                roomCategoryGroupService.getRoomCategoryGroups(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("room-category-groups-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
