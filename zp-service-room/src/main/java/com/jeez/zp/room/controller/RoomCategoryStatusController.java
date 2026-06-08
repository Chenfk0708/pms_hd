package com.jeez.zp.room.controller;

import com.jeez.zp.room.api.HudsonResponse;
import com.jeez.zp.room.api.TraceIdFactory;
import com.jeez.zp.room.dto.request.RoomCategorySaleStatusSaveRequest;
import com.jeez.zp.room.dto.request.RoomCategoryStatusRequest;
import com.jeez.zp.room.security.LoginUserContext;
import com.jeez.zp.room.service.RoomCategoryStatusService;
import com.jeez.zp.room.vo.RoomCategoryCentralStatusResponseVO;
import com.jeez.zp.room.vo.RoomCategoryChannelStatusResponseVO;
import com.jeez.zp.room.vo.RoomCategorySaleStatusSaveResponseVO;
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
                        request.getRoomCategoryIds(),
                        request.getPoiIds(),
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
                        request.getRoomCategoryIds(),
                        request.getPoiIds(),
                        request.getDate(),
                        request.getDays(),
                        request.getPageNum(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("room-category-statuses-room-category-channel-get")
        );
    }

    @PostMapping("/roomCategoryStatuses/central/saleStatus/save")
    public HudsonResponse<RoomCategorySaleStatusSaveResponseVO> saveCentralSaleStatus(@RequestBody RoomCategorySaleStatusSaveRequest request) {
        return HudsonResponse.success(
                roomCategoryStatusService.saveCentralSaleStatus(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getRoomCategoryId(),
                        request.getDate(),
                        request.getSaleEnabled()
                ),
                TraceIdFactory.next("room-category-statuses-central-sale-status-save")
        );
    }


    @PostMapping("/roomCategoryStatuses/roomCategory/get")
    public HudsonResponse<RoomCategoryCentralStatusResponseVO> getRetailStatuses(@RequestBody RoomCategoryStatusRequest request) {
        return HudsonResponse.success(
                roomCategoryStatusService.getRetailStatuses(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getRoomCategoryIds(),
                        request.getPoiIds(),
                        request.getDate(),
                        request.getDays(),
                        request.getPageNum(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("room-category-statuses-room-category-get")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
