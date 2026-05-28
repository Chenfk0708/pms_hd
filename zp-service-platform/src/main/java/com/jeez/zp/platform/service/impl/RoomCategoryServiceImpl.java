package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.RoomCategoryMapper;
import com.jeez.zp.platform.service.RoomCategoryService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.PoiViewVO;
import com.jeez.zp.platform.vo.RoomCategoryDetailChannelPriceRowVO;
import com.jeez.zp.platform.vo.RoomCategoryDetailChannelPriceVO;
import com.jeez.zp.platform.vo.RoomCategoryDetailResponseVO;
import com.jeez.zp.platform.vo.RoomCategoryDetailRowVO;
import com.jeez.zp.platform.vo.RoomCategoryPageItemVO;
import com.jeez.zp.platform.vo.RoomCategoryPageResponseVO;
import com.jeez.zp.platform.vo.RoomCategoryProductInfoVO;
import com.jeez.zp.platform.vo.RoomCategoryRoomViewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private final PlatformBootstrapMapper platformBootstrapMapper;

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
                normalizeChannelId(channelId)
        );

        List<RoomCategoryPageItemVO> items = total == 0
                ? List.of()
                : roomCategoryMapper.selectPage(
                resolvedCampId,
                poiId,
                roomCategoryGroupId,
                trimToNull(roomCategoryName),
                trimToNull(keyword),
                normalizeChannelId(channelId),
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

    @Override
    public RoomCategoryDetailResponseVO getDetail(Long roomCategoryId, Long userId) {
        if (roomCategoryId == null) {
            throw new BusinessException(40001, "房型ID不能为空");
        }

        Long resolvedCampId = resolveAccessibleCampId(null, userId);
        RoomCategoryDetailRowVO detailRow = roomCategoryMapper.selectDetail(resolvedCampId, roomCategoryId);
        if (detailRow == null) {
            throw new BusinessException(40404, "房型不存在");
        }

        int inventory = detailRow.getInventory() == null ? 0 : detailRow.getInventory();
        int staying = detailRow.getStaying() == null ? 0 : detailRow.getStaying();
        int pendingOrders = detailRow.getPendingOrders() == null ? 0 : detailRow.getPendingOrders();
        int occupancyRate = inventory <= 0 ? 0 : (int) Math.round((double) staying * 100 / inventory);

        List<RoomCategoryDetailChannelPriceVO> channelPrices = roomCategoryMapper
                .selectDetailChannelPriceRows(resolvedCampId, roomCategoryId)
                .stream()
                .map(this::toChannelPrice)
                .toList();

        RoomCategoryDetailResponseVO response = new RoomCategoryDetailResponseVO();
        response.setRoomName(detailRow.getRoomName());
        response.setOccupancyRate(occupancyRate);
        response.setInventory(inventory);
        response.setStaying(staying);
        response.setPendingOrders(pendingOrders);
        response.setChannelPrices(channelPrices);
        response.setGuidance(buildGuidance(detailRow.getRoomName(), inventory, staying, occupancyRate, pendingOrders, channelPrices.size()));
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

    private RoomCategoryDetailChannelPriceVO toChannelPrice(RoomCategoryDetailChannelPriceRowVO row) {
        RoomCategoryDetailChannelPriceVO item = new RoomCategoryDetailChannelPriceVO();
        item.setLabel(row.getLabel());
        item.setPrice(resolveChannelPrice(row));
        item.setStatus(resolveChannelPriceStatus(row));
        return item;
    }

    private Integer resolveChannelPrice(RoomCategoryDetailChannelPriceRowVO row) {
        String label = defaultString(row.getLabel());
        Long price = label.contains("美团") ? row.getWeekendPrice() : row.getBasePrice();
        return price == null ? 0 : Math.toIntExact(price);
    }

    private String resolveChannelPriceStatus(RoomCategoryDetailChannelPriceRowVO row) {
        boolean approved = "approved".equalsIgnoreCase(defaultString(row.getAuditStatus()));
        boolean onShelf = "on_shelf".equalsIgnoreCase(defaultString(row.getShelfStatus()));
        return approved && onShelf ? "已同步" : "待同步";
    }

    private List<String> buildGuidance(
            String roomName,
            int inventory,
            int staying,
            int occupancyRate,
            int pendingOrders,
            int channelPriceCount
    ) {
        List<String> guidance = new ArrayList<>();
        guidance.add("当前库存 " + inventory + " 间，在住 " + staying + " 间，入住压力 " + occupancyRate + "%。");
        guidance.add("待处理订单 " + pendingOrders + " 单，建议优先核对 " + roomName + " 的房态与渠道价差。");
        guidance.add("已关联 " + channelPriceCount + " 个渠道价格视图，后续可继续接入更多房型经营指标。");
        return guidance;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long normalizeChannelId(Long channelId) {
        if (channelId == null || channelId == 0L) {
            return null;
        }
        return channelId;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "当前用户上下文不存在");
        }
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!requestedCampId.equals(bundle.getCampId())) {
            throw new BusinessException(40301, "无权访问当前门店房型数据");
        }
        return requestedCampId;
    }
}
