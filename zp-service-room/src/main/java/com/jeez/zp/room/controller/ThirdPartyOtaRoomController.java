package com.jeez.zp.room.controller;

import com.jeez.zp.room.api.HudsonResponse;
import com.jeez.zp.room.api.TraceIdFactory;
import com.jeez.zp.room.dto.request.RoomCategoryPageRequest;
import com.jeez.zp.room.dto.request.RoomCategoryPricingRequest;
import com.jeez.zp.room.dto.request.RoomPageRequest;
import com.jeez.zp.room.dto.request.RoomStatusesMonthlyRequest;
import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.ThirdPartyOtaCatalogMapper;
import com.jeez.zp.room.security.ChannelCallbackAuthVerifier;
import com.jeez.zp.room.service.RoomCategoryPricingService;
import com.jeez.zp.room.service.RoomCategoryStatusService;
import com.jeez.zp.room.service.RoomCategoryService;
import com.jeez.zp.room.service.RoomService;
import com.jeez.zp.room.service.RoomStatusesMonthlyService;
import com.jeez.zp.room.vo.RoomCategoryPageResponseVO;
import com.jeez.zp.room.vo.RoomCategoryChannelStatusResponseVO;
import com.jeez.zp.room.vo.RoomPageResponseVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyInventoryVO;
import com.jeez.zp.room.vo.RoomStatusesMonthlyListResponseVO;
import com.jeez.zp.room.vo.ThirdPartyOtaPoiPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ThirdPartyOtaRoomController {

    private final ChannelCallbackAuthVerifier authVerifier;
    private final ThirdPartyOtaCatalogMapper catalogMapper;
    private final RoomCategoryService roomCategoryService;
    private final RoomService roomService;
    private final RoomStatusesMonthlyService roomStatusesMonthlyService;
    private final RoomCategoryPricingService roomCategoryPricingService;
    private final RoomCategoryStatusService roomCategoryStatusService;

    @PostMapping("/channelCallbacks/{channelCode}/pois/page/get")
    public HudsonResponse<ThirdPartyOtaPoiPageResponseVO> getPois(
            @PathVariable String channelCode,
            @RequestHeader(name = ChannelCallbackAuthVerifier.TEST_TOKEN_HEADER, required = false) String testToken,
            @RequestHeader(name = ChannelCallbackAuthVerifier.OPERATOR_ID_HEADER, required = false) String operatorId,
            @RequestBody RoomCategoryPageRequest request
    ) {
        authVerifier.verifyAndGetOperatorId(testToken, operatorId);
        Long campId = parseRequiredLong(request.getCampId(), "campId");
        int pageNum = normalizePageNum(request.getPageNum() != null ? request.getPageNum() : request.getCurrent());
        int pageSize = normalizePageSize(request.getPageSize());
        long total = catalogMapper.countPois(campId, channelCode, request.getKeyword());
        ThirdPartyOtaPoiPageResponseVO response = new ThirdPartyOtaPoiPageResponseVO();
        response.setTotal(total);
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setPages((int) Math.ceil(total / (double) pageSize));
        response.setHasNextPage((long) pageNum * pageSize < total);
        response.setList(catalogMapper.selectPois(campId, channelCode, request.getKeyword(), (long) (pageNum - 1) * pageSize, pageSize));
        return HudsonResponse.success(response, TraceIdFactory.next("third-party-ota-pois-page-get"));
    }

    @PostMapping("/channelCallbacks/{channelCode}/roomCategories/page/get")
    public HudsonResponse<RoomCategoryPageResponseVO> getRoomCategories(
            @PathVariable String channelCode,
            @RequestHeader(name = ChannelCallbackAuthVerifier.TEST_TOKEN_HEADER, required = false) String testToken,
            @RequestHeader(name = ChannelCallbackAuthVerifier.OPERATOR_ID_HEADER, required = false) String operatorId,
            @RequestBody RoomCategoryPageRequest request
    ) {
        Long operatorUserId = authVerifier.verifyAndGetOperatorId(testToken, operatorId);
        return HudsonResponse.success(
                roomCategoryService.getPage(
                        parseLong(request.getCampId()),
                        operatorUserId,
                        parseLong(request.getPoiId()),
                        parseLong(request.getRoomCategoryGroupId()),
                        request.getRoomCategoryName(),
                        request.getKeyword(),
                        parseLong(request.getChannelId()),
                        request.getPageNum(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("third-party-ota-room-categories-page-get")
        );
    }

    @PostMapping("/channelCallbacks/{channelCode}/rooms/page/get")
    public HudsonResponse<RoomPageResponseVO> getRooms(
            @PathVariable String channelCode,
            @RequestHeader(name = ChannelCallbackAuthVerifier.TEST_TOKEN_HEADER, required = false) String testToken,
            @RequestHeader(name = ChannelCallbackAuthVerifier.OPERATOR_ID_HEADER, required = false) String operatorId,
            @RequestBody RoomPageRequest request
    ) {
        Long operatorUserId = authVerifier.verifyAndGetOperatorId(testToken, operatorId);
        return HudsonResponse.success(
                roomService.getRoomsPage(
                        parseLong(request.getCampId()),
                        operatorUserId,
                        parseLong(request.getPoiId()),
                        parseLong(request.getStoreId()),
                        parseLongList(request.getRoomCategoryIds()),
                        request.getIsAvailability(),
                        request.getSaleType(),
                        request.getKeyword(),
                        request.getPageNum(),
                        request.getCurrent(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("third-party-ota-rooms-page-get")
        );
    }

    @PostMapping("/channelCallbacks/{channelCode}/inventory/get")
    public HudsonResponse<RoomStatusesMonthlyListResponseVO<RoomStatusesMonthlyInventoryVO>> getInventory(
            @PathVariable String channelCode,
            @RequestHeader(name = ChannelCallbackAuthVerifier.TEST_TOKEN_HEADER, required = false) String testToken,
            @RequestHeader(name = ChannelCallbackAuthVerifier.OPERATOR_ID_HEADER, required = false) String operatorId,
            @RequestBody RoomStatusesMonthlyRequest request
    ) {
        Long operatorUserId = authVerifier.verifyAndGetOperatorId(testToken, operatorId);
        return HudsonResponse.success(
                roomStatusesMonthlyService.getInventory(
                        parseLong(request.getCampId()),
                        operatorUserId,
                        request.getStartDate(),
                        request.getDays(),
                        parseLongList(request.getRoomCategoryIds()),
                        parseLongList(request.getPoiIds()),
                        parseLong(request.getStoreId()),
                        request.getQueryCode()
                ),
                TraceIdFactory.next("third-party-ota-inventory-get")
        );
    }

    @PostMapping("/channelCallbacks/{channelCode}/rates/get")
    public HudsonResponse<RoomCategoryChannelStatusResponseVO> getRates(
            @PathVariable String channelCode,
            @RequestHeader(name = ChannelCallbackAuthVerifier.TEST_TOKEN_HEADER, required = false) String testToken,
            @RequestHeader(name = ChannelCallbackAuthVerifier.OPERATOR_ID_HEADER, required = false) String operatorId,
            @RequestBody RoomCategoryPricingRequest request
    ) {
        Long operatorUserId = authVerifier.verifyAndGetOperatorId(testToken, operatorId);
        return HudsonResponse.success(
                roomCategoryStatusService.getChannelStatuses(
                        parseLong(request.getCampId()),
                        operatorUserId,
                        request.getChannelIds(),
                        request.getRoomCategoryIds(),
                        request.getPoiIds(),
                        resolveRateStartDate(request),
                        request.getDays(),
                        request.getPageNum(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("third-party-ota-rates-get")
        );
    }

    private String resolveRateStartDate(RoomCategoryPricingRequest request) {
        if (request.getDate() != null && !request.getDate().isBlank()) {
            return request.getDate();
        }
        return request.getStartDate();
    }

    private Long parseRequiredLong(String value, String fieldName) {
        Long parsed = parseLong(value);
        if (parsed == null) {
            throw new BusinessException(40001, "缺少必填字段: " + fieldName);
        }
        return parsed;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }

    private java.util.List<Long> parseLongList(java.util.List<String> values) {
        if (values == null || values.isEmpty()) {
            return java.util.List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(Long::valueOf)
                .toList();
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 200);
    }
}
