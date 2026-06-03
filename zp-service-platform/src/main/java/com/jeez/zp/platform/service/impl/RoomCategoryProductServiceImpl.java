package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.RoomCategoryProductMapper;
import com.jeez.zp.platform.service.RoomCategoryProductService;
import com.jeez.zp.platform.vo.ChannelRoomCategoryPageItemVO;
import com.jeez.zp.platform.vo.ChannelRoomCategoryPageResponseVO;
import com.jeez.zp.platform.vo.ChannelRoomCategoryPageRowVO;
import com.jeez.zp.platform.vo.ChannelRoomCategoryProductVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.PaginationVO;
import com.jeez.zp.platform.vo.RoomCategoryProductPageItemVO;
import com.jeez.zp.platform.vo.RoomCategoryProductPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomCategoryProductServiceImpl implements RoomCategoryProductService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final RoomCategoryProductMapper roomCategoryProductMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public RoomCategoryProductPageResponseVO getPage(
            Long campId,
            Long userId,
            String keyword,
            Long roomCategoryId,
            Long channelId,
            Integer pageNum,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int resolvedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        long offset = (long) (resolvedPageNum - 1) * resolvedPageSize;

        long total = roomCategoryProductMapper.countPage(
                resolvedCampId,
                trimToNull(keyword),
                roomCategoryId,
                normalizeChannelId(channelId)
        );
        List<RoomCategoryProductPageItemVO> items = total == 0
                ? List.of()
                : roomCategoryProductMapper.selectPage(
                resolvedCampId,
                trimToNull(keyword),
                roomCategoryId,
                normalizeChannelId(channelId),
                offset,
                resolvedPageSize
        );

        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / resolvedPageSize);

        RoomCategoryProductPageResponseVO response = new RoomCategoryProductPageResponseVO();
        response.setTotal(total);
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setExtraInfo(null);
        response.setPageNum(resolvedPageNum);
        response.setHasNextPage(resolvedPageNum < pages);
        response.setPages(pages);
        response.setList(items);
        response.setPagination(new PaginationVO(resolvedPageNum, resolvedPageSize, Math.toIntExact(total)));
        return response;
    }


    @Override
    public ChannelRoomCategoryPageResponseVO getChannelRoomCategoryPageV2(
            Long campId,
            Long userId,
            List<Integer> roomCategoryTypes,
            List<Long> categoryIds,
            String keyword,
            List<Long> channelIds,
            List<Long> poiIds,
            List<String> shelfStatuses,
            Integer pageNum,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int resolvedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        long offset = (long) (resolvedPageNum - 1) * resolvedPageSize;

        List<Integer> resolvedRoomCategoryTypes = normalizeIntegers(roomCategoryTypes);
        List<Long> resolvedCategoryIds = normalizeIds(categoryIds);
        List<Long> resolvedChannelIds = normalizeIds(channelIds);
        List<Long> resolvedPoiIds = normalizeIds(poiIds);
        List<String> resolvedShelfStatuses = normalizeShelfStatuses(shelfStatuses);
        String resolvedKeyword = trimToNull(keyword);

        long total = roomCategoryProductMapper.countChannelRoomCategoryPageV2(
                resolvedCampId,
                resolvedRoomCategoryTypes,
                resolvedCategoryIds,
                resolvedKeyword,
                resolvedChannelIds,
                resolvedPoiIds,
                resolvedShelfStatuses
        );
        List<ChannelRoomCategoryPageRowVO> rows = total == 0
                ? List.of()
                : roomCategoryProductMapper.selectChannelRoomCategoryPageV2(
                resolvedCampId,
                resolvedRoomCategoryTypes,
                resolvedCategoryIds,
                resolvedKeyword,
                resolvedChannelIds,
                resolvedPoiIds,
                resolvedShelfStatuses,
                offset,
                resolvedPageSize
        );

        Map<Long, List<ChannelRoomCategoryProductVO>> productsByGoodsId = loadChannelRoomCategoryProducts(rows);
        List<ChannelRoomCategoryPageItemVO> items = rows.stream()
                .map(row -> toChannelRoomCategoryItem(row, productsByGoodsId.getOrDefault(row.getGoodsId(), List.of())))
                .toList();

        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / resolvedPageSize);
        ChannelRoomCategoryPageResponseVO response = new ChannelRoomCategoryPageResponseVO();
        response.setTotal(total);
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setExtraInfo(null);
        response.setPageNum(resolvedPageNum);
        response.setHasNextPage(resolvedPageNum < pages);
        response.setPages(pages);
        response.setList(items);
        response.setPagination(new PaginationVO(resolvedPageNum, resolvedPageSize, Math.toIntExact(total)));
        return response;
    }

    private Map<Long, List<ChannelRoomCategoryProductVO>> loadChannelRoomCategoryProducts(List<ChannelRoomCategoryPageRowVO> rows) {
        if (rows.isEmpty()) {
            return Map.of();
        }
        List<Long> goodsIds = rows.stream()
                .map(ChannelRoomCategoryPageRowVO::getGoodsId)
                .filter(Objects::nonNull)
                .toList();
        if (goodsIds.isEmpty()) {
            return Map.of();
        }
        return roomCategoryProductMapper.selectChannelRoomCategoryProductRows(goodsIds).stream()
                .collect(Collectors.groupingBy(
                        ChannelRoomCategoryProductVO::getGoodsIdValue,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private ChannelRoomCategoryPageItemVO toChannelRoomCategoryItem(
            ChannelRoomCategoryPageRowVO row,
            List<ChannelRoomCategoryProductVO> products
    ) {
        String shelfStatus = row.getRawShelfStatus() == null ? "warehouse" : row.getRawShelfStatus();
        int totalStock = row.getTotalStock() == null ? 0 : row.getTotalStock();
        int isCanBooking = "selling".equals(shelfStatus) ? 1 : 0;

        ChannelRoomCategoryPageItemVO item = new ChannelRoomCategoryPageItemVO();
        item.setChannelRoomCategoryId(String.valueOf(row.getGoodsId()));
        item.setChannelRoomCategoryName(row.getChannelRoomCategoryName());
        item.setCategoryId(row.getCategoryId() == null ? null : String.valueOf(row.getCategoryId()));
        item.setCategoryName(row.getCategoryName());
        item.setRoomCategoryType(row.getRoomCategoryType());
        item.setGoodsType(resolveGoodsTypeCode(row.getRawGoodsType()));
        item.setChannelIds(splitCsv(row.getChannelIdsCsv(), ","));
        item.setChannelNames(splitCsv(row.getChannelNamesCsv(), "\\|\\|"));
        item.setTotalStock(String.valueOf(totalStock));
        item.setSoldCount(row.getSoldCount() == null ? 0 : Math.toIntExact(row.getSoldCount()));
        item.setLowestSellingPrice(row.getLowestSellingPrice() == null ? 0L : row.getLowestSellingPrice());
        item.setLowestOriginalPrice(row.getLowestOriginalPrice() == null ? 0L : row.getLowestOriginalPrice());
        item.setIsCanBooking(isCanBooking);
        item.setIsAvailability(String.valueOf(isCanBooking));
        item.setShelfStatus(shelfStatus);
        item.setCreatedAt(row.getCreatedAt());
        item.setUpdatedAt(row.getUpdatedAt());
        item.setDescription(row.getDescription());
        item.setRefundRule(row.getRefundRule());
        item.setProducts(products == null ? List.of() : products);
        return item;
    }

    private List<Long> normalizeIds(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && value > 0)
                .distinct()
                .toList();
    }

    private List<Integer> normalizeIntegers(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && value > 0)
                .distinct()
                .toList();
    }

    private List<String> normalizeShelfStatuses(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> value.equals("selling") || value.equals("soldOut") || value.equals("warehouse"))
                .distinct()
                .toList();
    }

    private List<String> splitCsv(String value, String regex) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(regex))
                .filter(part -> part != null && !part.isBlank())
                .toList();
    }

    private int resolveGoodsTypeCode(String rawGoodsType) {
        if (rawGoodsType == null || rawGoodsType.isBlank()) {
            return 7;
        }
        String trimmed = rawGoodsType.trim();
        if (trimmed.chars().allMatch(Character::isDigit)) {
            return Integer.parseInt(trimmed);
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        if (normalized.contains("edition") || normalized.contains("version")) {
            return 2;
        }
        if (normalized.contains("locals") || normalized.contains("mall") || normalized.contains("hardware")) {
            return 6;
        }
        return 7;
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

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "当前用户上下文不存在");
        }
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!requestedCampId.equals(bundle.getCampId())) {
            throw new BusinessException(40301, "无权访问当前门店酒店套餐数据");
        }
        return requestedCampId;
    }
}
