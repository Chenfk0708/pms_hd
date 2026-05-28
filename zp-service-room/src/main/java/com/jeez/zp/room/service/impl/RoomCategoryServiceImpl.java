package com.jeez.zp.room.service.impl;

import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.RoomCategoryMapper;
import com.jeez.zp.room.mapper.UserCampMapper;
import com.jeez.zp.room.service.RoomCategoryService;
import com.jeez.zp.room.vo.PoiViewVO;
import com.jeez.zp.room.vo.RoomCategoryPageItemVO;
import com.jeez.zp.room.vo.RoomCategoryPageResponseVO;
import com.jeez.zp.room.vo.RoomCategoryProductInfoVO;
import com.jeez.zp.room.vo.RoomCategoryRoomViewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomCategoryServiceImpl implements RoomCategoryService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int DEFAULT_SALE_TYPE = 1;
    private static final int DEFAULT_CANCEL_POLICY = 0;
    private static final int DEFAULT_BREAKFAST_COUNT = 0;
    private static final int DEFAULT_IS_PERFECT_PRODUCT = 1;

    private final RoomCategoryMapper roomCategoryMapper;
    private final UserCampMapper userCampMapper;

    @Override
    public RoomCategoryPageResponseVO getPage(
            Long campId,
            Long userId,
            Long poiId,
            Long roomCategoryGroupId,
            String roomCategoryName,
            String keyword,
            Long channelId,
            Integer pageNum,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int resolvedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        long offset = (long) (resolvedPageNum - 1) * resolvedPageSize;

        long total = roomCategoryMapper.countPage(
                resolvedCampId,
                poiId,
                roomCategoryGroupId,
                trimToNull(roomCategoryName),
                trimToNull(keyword),
                channelId
        );

        List<RoomCategoryPageItemVO> items = total == 0
                ? List.of()
                : roomCategoryMapper.selectPage(
                resolvedCampId,
                poiId,
                roomCategoryGroupId,
                trimToNull(roomCategoryName),
                trimToNull(keyword),
                channelId,
                offset,
                resolvedPageSize
        );

        hydrateNestedViews(items);

        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / resolvedPageSize);

        RoomCategoryPageResponseVO response = new RoomCategoryPageResponseVO();
        response.setTotal(total);
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setExtraInfo(null);
        response.setPageNum(resolvedPageNum);
        response.setHasNextPage(resolvedPageNum < pages);
        response.setPages(pages);
        response.setList(items);
        return response;
    }

    private void hydrateNestedViews(List<RoomCategoryPageItemVO> items) {
        if (items.isEmpty()) {
            return;
        }

        List<Long> roomCategoryIds = items.stream()
                .map(RoomCategoryPageItemVO::getRoomCategoryId)
                .filter(Objects::nonNull)
                .map(Long::valueOf)
                .toList();

        Map<String, List<RoomCategoryRoomViewVO>> roomViewsByCategoryId = roomCategoryMapper.selectRoomViews(roomCategoryIds)
                .stream()
                .collect(Collectors.groupingBy(
                        RoomCategoryRoomViewVO::getRoomCategoryId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        for (RoomCategoryPageItemVO item : items) {
            item.setRoomViews(roomViewsByCategoryId.getOrDefault(item.getRoomCategoryId(), List.of()));
            item.setPoiView(buildPoiView(item));
            item.setRoomCategoryProductInfoViews(List.of(buildProductInfo(item)));
            item.setRoomCategorySystemConfigInfos(List.of());
            item.setLinkRcs(List.of());
            item.setByLinkRcs(List.of());
            item.setChannels(List.of());
            item.setIcsInfoViews(List.of());
            item.setSaleCampId(item.getCampId());
            item.setUpstreamCampId(item.getCampId());
            item.setDownstreamCampId(item.getCampId());
            item.setFromType(0);
            item.setChannelId("0");
            item.setParentId(item.getRoomCategoryId());
            item.setIsTransfer(0);
            item.setChannelOrderTotalNum(0);
            item.setExpectedChannelOrderTotalNum(0);
            item.setIsAvailability(1);
            item.setWaitMappingChannelNum(0);
            item.setIsSupportHotelProduct(1);
            item.setIsCanDelete(1);
            if (item.getInventory() == null && item.getRoomNum() != null) {
                item.setInventory(item.getRoomNum());
            }
        }
    }

    private PoiViewVO buildPoiView(RoomCategoryPageItemVO item) {
        PoiViewVO poiView = new PoiViewVO();
        poiView.setPoiId(item.getPoiId());
        poiView.setName(item.getPoiName());
        return poiView;
    }

    private RoomCategoryProductInfoVO buildProductInfo(RoomCategoryPageItemVO item) {
        RoomCategoryProductInfoVO productInfo = new RoomCategoryProductInfoVO();
        productInfo.setRoomCategoryProductId(item.getRoomCategoryId());
        productInfo.setRoomCategoryProductName(item.getRoomCategoryName());
        productInfo.setSaleType(DEFAULT_SALE_TYPE);
        productInfo.setSerialCheckInTime(null);
        productInfo.setEarliestCheckInTime(item.getEarliestCheckInTime());
        productInfo.setLatestCheckInTime(item.getLatestCheckInTime());
        productInfo.setLatestCheckOutTime(item.getLatestCheckOutTime());
        productInfo.setHourCheckInTime(null);
        productInfo.setHourCheckOutTime(null);
        productInfo.setHourSerialCheckTime(null);
        productInfo.setIsHourLimit(null);
        productInfo.setCancelPolicy(DEFAULT_CANCEL_POLICY);
        productInfo.setBreakfastCount(DEFAULT_BREAKFAST_COUNT);
        productInfo.setIsPerfectRoomCategoryProduct(DEFAULT_IS_PERFECT_PRODUCT);
        productInfo.setNormalPrice(item.getBasePrice());
        productInfo.setWeekendPrice(item.getWeekendPrice());
        productInfo.setHolidayPrice(item.getHolidayPrice());
        productInfo.setStockMode(null);
        return productInfo;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        Long currentCampId = userCampMapper.selectCurrentCampId(userId);
        if (currentCampId == null) {
            throw new BusinessException(40401, "未找到当前用户门店");
        }
        if (requestedCampId == null) {
            return currentCampId;
        }
        if (!requestedCampId.equals(currentCampId)) {
            throw new BusinessException(40301, "无权访问当前门店房型数据");
        }
        return requestedCampId;
    }
}
