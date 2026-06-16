package com.jeez.zp.room.controller;

import com.jeez.zp.room.api.HudsonResponse;
import com.jeez.zp.room.api.TraceIdFactory;
import com.jeez.zp.room.dto.request.RoomStatusCloseRequest;
import com.jeez.zp.room.dto.request.RoomStatusCleanRequest;
import com.jeez.zp.room.dto.request.RoomStatusesMonthlyRequest;
import com.jeez.zp.room.security.LoginUserContext;
import com.jeez.zp.room.service.RoomStatusesMonthlyService;
import com.jeez.zp.room.vo.RoomStatusCleanResponseVO;
import com.jeez.zp.room.vo.RoomStatusCloseResponseVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyBlockVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyDailyMonitorVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyInventoryVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyListResponseVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyOccVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyOrderDetailsResponseVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyRedDotVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomStatusesMonthlyController {

    private final RoomStatusesMonthlyService roomStatusesMonthlyService;

    @PostMapping("/roomStatuses/close/save")
    public HudsonResponse<RoomStatusCloseResponseVO> closeRoom(@RequestBody RoomStatusCloseRequest request) {
        return HudsonResponse.success(
                roomStatusesMonthlyService.closeRoom(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("room-statuses-close-save")
        );
    }

    @PostMapping("/roomStatuses/open/save")
    public HudsonResponse<RoomStatusCloseResponseVO> openRoom(@RequestBody RoomStatusCloseRequest request) {
        return HudsonResponse.success(
                roomStatusesMonthlyService.openRoom(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("room-statuses-open-save")
        );
    }

    @PostMapping("/roomStatuses/clean/save")
    public HudsonResponse<RoomStatusCleanResponseVO> saveRoomCleanStatus(@RequestBody RoomStatusCleanRequest request) {
        return HudsonResponse.success(
                roomStatusesMonthlyService.saveRoomCleanStatus(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("room-statuses-clean-save")
        );
    }

    @PostMapping("/roomStatuses/inv/get")
    public HudsonResponse<RoomStatusesMonthlyListResponseVO<RoomStatusesMonthlyInventoryVO>> getInventory(
            @RequestBody RoomStatusesMonthlyRequest request
    ) {
        return HudsonResponse.success(
                roomStatusesMonthlyService.getInventory(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getStartDate(),
                        request.getDays(),
                        parseLongList(request.getRoomCategoryIds()),
                        parseLongList(request.getPoiIds()),
                        parseLong(request.getStoreId()),
                        request.getQueryCode()
                ),
                TraceIdFactory.next("room-statuses-inv-get")
        );
    }

    @PostMapping("/roomStatuses/dailyMonitor/get")
    public HudsonResponse<RoomStatusesMonthlyListResponseVO<RoomStatusesMonthlyDailyMonitorVO>> getDailyMonitor(
            @RequestBody RoomStatusesMonthlyRequest request
    ) {
        return HudsonResponse.success(
                roomStatusesMonthlyService.getDailyMonitor(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getStartDate(),
                        request.getDays(),
                        parseLongList(request.getRoomCategoryIds()),
                        parseLongList(request.getPoiIds()),
                        parseLong(request.getStoreId()),
                        request.getQueryCode()
                ),
                TraceIdFactory.next("room-statuses-daily-monitor-get")
        );
    }

    @PostMapping("/roomStatuses/occ/get")
    public HudsonResponse<RoomStatusesMonthlyListResponseVO<RoomStatusesMonthlyOccVO>> getOcc(
            @RequestBody RoomStatusesMonthlyRequest request
    ) {
        return HudsonResponse.success(
                roomStatusesMonthlyService.getOcc(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getStartDate(),
                        request.getDays(),
                        parseLongList(request.getRoomCategoryIds()),
                        parseLongList(request.getPoiIds()),
                        parseLong(request.getStoreId()),
                        request.getQueryCode()
                ),
                TraceIdFactory.next("room-statuses-occ-get")
        );
    }

    @PostMapping("/roomStatuses/block/get")
    public HudsonResponse<RoomStatusesMonthlyListResponseVO<RoomStatusesMonthlyBlockVO>> getBlock(
            @RequestBody RoomStatusesMonthlyRequest request
    ) {
        return HudsonResponse.success(
                roomStatusesMonthlyService.getBlock(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getStartDate(),
                        request.getDays(),
                        parseLongList(request.getRoomCategoryIds()),
                        parseLongList(request.getPoiIds()),
                        parseLong(request.getStoreId()),
                        request.getQueryCode()
                ),
                TraceIdFactory.next("room-statuses-block-get")
        );
    }

    @PostMapping("/roomStatuses/redDot/get")
    public HudsonResponse<RoomStatusesMonthlyListResponseVO<RoomStatusesMonthlyRedDotVO>> getRedDot(
            @RequestBody RoomStatusesMonthlyRequest request
    ) {
        return HudsonResponse.success(
                roomStatusesMonthlyService.getRedDot(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getStartDate(),
                        request.getDays(),
                        parseLongList(request.getRoomCategoryIds()),
                        parseLongList(request.getPoiIds()),
                        parseLong(request.getStoreId()),
                        request.getQueryCode()
                ),
                TraceIdFactory.next("room-statuses-red-dot-get")
        );
    }

    @PostMapping("/roomStatuses/orderDetails/get")
    public HudsonResponse<RoomStatusesMonthlyOrderDetailsResponseVO> getOrderDetails(
            @RequestBody RoomStatusesMonthlyRequest request
    ) {
        return HudsonResponse.success(
                roomStatusesMonthlyService.getOrderDetails(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getStartDate(),
                        request.getDays(),
                        parseLongList(request.getRoomCategoryIds()),
                        parseLongList(request.getPoiIds()),
                        parseLong(request.getStoreId()),
                        request.getQueryCode(),
                        request.getPage(),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("room-statuses-order-details-get")
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

    private List<Long> parseLongList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(this::parseLong)
                .filter(value -> value != null)
                .toList();
    }
}
