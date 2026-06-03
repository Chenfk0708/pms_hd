package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.RoomCategoryDeleteRequest;
import com.jeez.zp.platform.dto.request.RoomCategoryDetailRequest;
import com.jeez.zp.platform.dto.request.RoomCategoryLinkageRequest;
import com.jeez.zp.platform.dto.request.RoomCategoryPageRequest;
import com.jeez.zp.platform.dto.request.RoomCategorySaveRequest;
import com.jeez.zp.platform.dto.request.SelectRoomCategoryPageRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.RoomCategoryService;
import com.jeez.zp.platform.vo.RoomCategoryDetailResponseVO;
import com.jeez.zp.platform.vo.RoomCategoryEditDraftVO;
import com.jeez.zp.platform.vo.RoomCategoryLinkageResponseVO;
import com.jeez.zp.platform.vo.RoomCategoryMutationResultVO;
import com.jeez.zp.platform.vo.RoomCategoryPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomCategoryController {

    private final RoomCategoryService roomCategoryService;

    @PostMapping("/roomCategories/page/get")
    public HudsonResponse<RoomCategoryPageResponseVO> getPage(@RequestBody RoomCategoryPageRequest request) {
        return HudsonResponse.success(
                roomCategoryService.getPage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLong(request.getPoiId()),
                        parseLong(request.getRoomCategoryGroupId()),
                        request.getRoomCategoryName(),
                        request.getKeyword(),
                        parseLong(request.getChannelId()),
                        request.getPageNum(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("room-categories-page-get")
        );
    }

    @PostMapping("/select/roomCategory/page/get")
    public HudsonResponse<RoomCategoryPageResponseVO> getSelectPage(@RequestBody SelectRoomCategoryPageRequest request) {
        return HudsonResponse.success(
                roomCategoryService.getPage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLong(request.getPoiId()),
                        null,
                        null,
                        null,
                        parseSelectChannelId(request),
                        request.getPageNum() != null ? request.getPageNum() : request.getCurrent(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("select-room-category-page-get")
        );
    }

    @PostMapping("/roomCategories/detail/get")
    public HudsonResponse<RoomCategoryDetailResponseVO> getDetail(@RequestBody RoomCategoryDetailRequest request) {
        return HudsonResponse.success(
                roomCategoryService.getDetail(parseLong(request.getRoomCategoryId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("room-categories-detail-get")
        );
    }

    @PostMapping("/roomCategory/detail/get")
    public HudsonResponse<RoomCategoryEditDraftVO> getEditDetail(@RequestBody RoomCategoryDetailRequest request) {
        return HudsonResponse.success(
                roomCategoryService.getEditDetail(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLong(request.getRoomCategoryId()),
                        request.getMode()
                ),
                TraceIdFactory.next("room-category-detail-get")
        );
    }

    @PostMapping("/roomCategory/linkage/get")
    public HudsonResponse<RoomCategoryLinkageResponseVO> getLinkage(@RequestBody RoomCategoryLinkageRequest request) {
        return HudsonResponse.success(
                roomCategoryService.getLinkage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLong(request.getRoomCategoryId())
                ),
                TraceIdFactory.next("room-category-linkage-get")
        );
    }

    @PostMapping("/roomCategory/linkage/save")
    public HudsonResponse<RoomCategoryMutationResultVO> saveLinkage(@RequestBody RoomCategoryLinkageRequest request) {
        return HudsonResponse.success(
                roomCategoryService.saveLinkage(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLong(request.getRoomCategoryId()),
                        parseLongList(request.getLinkedRoomCategoryIds())
                ),
                TraceIdFactory.next("room-category-linkage-save")
        );
    }

    @PostMapping("/roomCategory/save")
    public HudsonResponse<RoomCategoryMutationResultVO> saveRoomCategory(@RequestBody RoomCategorySaveRequest request) {
        return HudsonResponse.success(
                roomCategoryService.saveRoomCategory(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getForm()
                ),
                TraceIdFactory.next("room-category-save")
        );
    }

    @PostMapping("/roomCategory/delete")
    public HudsonResponse<RoomCategoryMutationResultVO> deleteRoomCategory(@RequestBody RoomCategoryDeleteRequest request) {
        return HudsonResponse.success(
                roomCategoryService.deleteRoomCategory(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseLong(request.getRoomCategoryId())
                ),
                TraceIdFactory.next("room-category-delete")
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

    private Long parseSelectChannelId(SelectRoomCategoryPageRequest request) {
        Long channelId = normalizeZeroToNull(parseLong(request.getChannelId()));
        if (channelId != null) {
            return channelId;
        }
        return normalizeZeroToNull(parseLong(request.getFilterSyncChannelId()));
    }

    private Long normalizeZeroToNull(Long value) {
        if (value == null || value == 0L) {
            return null;
        }
        return value;
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
