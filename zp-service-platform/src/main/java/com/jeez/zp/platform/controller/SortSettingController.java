package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.ChannelRoomCategorySeqsRequest;
import com.jeez.zp.platform.dto.request.RoomCategorySeqsRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.SortSettingService;
import com.jeez.zp.platform.vo.SortSettingMutationResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SortSettingController {

    private final SortSettingService sortSettingService;

    @PostMapping("/roomCategory/seqs")
    public HudsonResponse<SortSettingMutationResultVO> updateRoomCategorySeqs(@RequestBody RoomCategorySeqsRequest request) {
        return HudsonResponse.success(
                sortSettingService.updateRoomCategorySeqs(
                        parseLong(request == null ? null : request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request
                ),
                TraceIdFactory.next("room-category-seqs")
        );
    }

    @PostMapping("/channelRoomCategories/seqs")
    public HudsonResponse<SortSettingMutationResultVO> updateChannelRoomCategorySeqs(@RequestBody ChannelRoomCategorySeqsRequest request) {
        return HudsonResponse.success(
                sortSettingService.updateChannelRoomCategorySeqs(
                        parseLong(request == null ? null : request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request
                ),
                TraceIdFactory.next("channel-room-categories-seqs")
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
