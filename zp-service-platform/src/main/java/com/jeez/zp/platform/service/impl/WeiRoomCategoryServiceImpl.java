package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.WeiRoomCategoryMapper;
import com.jeez.zp.platform.service.WeiRoomCategoryService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.WeiRoomCategoryCatalogRowVO;
import com.jeez.zp.platform.vo.WeiRoomCategoryPageItemVO;
import com.jeez.zp.platform.vo.WeiRoomCategoryPageResponseVO;
import com.jeez.zp.platform.vo.WeiRoomCategoryProductGetVO;
import com.jeez.zp.platform.vo.WeiRoomCategoryProductRowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeiRoomCategoryServiceImpl implements WeiRoomCategoryService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int DEFAULT_GOODS_TYPE = 7;
    private static final int VERSION_GOODS_TYPE = 2;
    private static final int LOCALS_MALL_GOODS_TYPE = 6;

    private final WeiRoomCategoryMapper weiRoomCategoryMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public WeiRoomCategoryPageResponseVO getPage(
            Long catalogCampId,
            Long buyCampId,
            Long userId,
            List<Integer> roomCategoryTypes,
            List<Integer> goodsTypes,
            Integer pageNum,
            Integer pageSize,
            String keyword
    ) {
        resolveAccessibleBuyCampId(buyCampId, userId);

        int resolvedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int resolvedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;

        List<WeiRoomCategoryCatalogRowVO> rows = catalogCampId == null
                ? List.of()
                : weiRoomCategoryMapper.selectCatalogRows(catalogCampId, roomCategoryTypes, trimToNull(keyword));

        List<WeiRoomCategoryCatalogRowVO> filteredRows = rows.stream()
                .filter(row -> matchesGoodsTypes(resolveGoodsTypeCode(row.getRawGoodsType()), goodsTypes))
                .sorted((left, right) -> Long.compare(left.getGoodsId(), right.getGoodsId()))
                .toList();

        long total = filteredRows.size();
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / resolvedPageSize);
        int fromIndex = Math.min((resolvedPageNum - 1) * resolvedPageSize, filteredRows.size());
        int toIndex = Math.min(fromIndex + resolvedPageSize, filteredRows.size());
        List<WeiRoomCategoryCatalogRowVO> pageRows = filteredRows.subList(fromIndex, toIndex);

        Map<Long, List<WeiRoomCategoryProductGetVO>> productsByGoodsId = loadProducts(pageRows);

        List<WeiRoomCategoryPageItemVO> items = pageRows.stream()
                .map(row -> toPageItem(row, resolveGoodsTypeCode(row.getRawGoodsType()), productsByGoodsId.getOrDefault(row.getGoodsId(), List.of())))
                .toList();

        WeiRoomCategoryPageResponseVO response = new WeiRoomCategoryPageResponseVO();
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

    private Map<Long, List<WeiRoomCategoryProductGetVO>> loadProducts(List<WeiRoomCategoryCatalogRowVO> pageRows) {
        if (pageRows.isEmpty()) {
            return Map.of();
        }
        List<Long> goodsIds = pageRows.stream().map(WeiRoomCategoryCatalogRowVO::getGoodsId).toList();
        return weiRoomCategoryMapper.selectProductRows(goodsIds).stream()
                .map(this::toProductVO)
                .collect(Collectors.groupingBy(
                        WeiRoomCategoryProductGetVO::getGoodsIdValue,
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(Collectors.toList(), products -> {
                            products.forEach(product -> product.setGoodsIdValue(null));
                            return products;
                        })
                ));
    }

    private WeiRoomCategoryPageItemVO toPageItem(
            WeiRoomCategoryCatalogRowVO row,
            int goodsTypeCode,
            List<WeiRoomCategoryProductGetVO> products
    ) {
        List<WeiRoomCategoryProductGetVO> safeProducts = products == null ? List.of() : products;

        long lowestSellingPrice = safeProducts.stream()
                .map(WeiRoomCategoryProductGetVO::getSellingPrice)
                .filter(Objects::nonNull)
                .min(Long::compareTo)
                .orElse(defaultLong(row.getDefaultSellingPrice()));
        long lowestOriginalPrice = safeProducts.stream()
                .map(WeiRoomCategoryProductGetVO::getOriginalPrice)
                .filter(Objects::nonNull)
                .min(Long::compareTo)
                .orElse(defaultLong(row.getDefaultOriginalPrice()));
        long lowestSettlementPrice = safeProducts.stream()
                .map(WeiRoomCategoryProductGetVO::getSettlementPrice)
                .filter(Objects::nonNull)
                .min(Long::compareTo)
                .orElse(defaultLong(row.getDefaultSettlementPrice()));
        int totalStock = safeProducts.stream()
                .map(WeiRoomCategoryProductGetVO::getStock)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        int isCanBooking = safeProducts.stream()
                .map(WeiRoomCategoryProductGetVO::getIsCanBooking)
                .filter(Objects::nonNull)
                .anyMatch(value -> value == 1)
                ? 1
                : deriveCanBooking(row.getShelfStatus(), row.getStockMode(), totalStock);

        WeiRoomCategoryPageItemVO item = new WeiRoomCategoryPageItemVO();
        item.setCampId(null);
        item.setChannelRoomCategoryId(String.valueOf(row.getGoodsId()));
        item.setChannelRoomCategoryName(row.getChannelRoomCategoryName());
        item.setParentRoomCategoryId(row.getParentRoomCategoryId());
        item.setMainPhotoMediaId(row.getMainPhotoMediaId());
        item.setMainPhotoMediaUrl(row.getMainPhotoMediaUrl());
        item.setMainPhoto(row.getMainPhoto());
        item.setBedroomNum(null);
        item.setAbleArea(null);
        item.setHallNum(null);
        item.setPersonCapacity(null);
        item.setBedNum(null);
        item.setBedMetas(List.of());
        item.setLowestSellingPrice(lowestSellingPrice);
        item.setLowestSettlementPrice(lowestSettlementPrice);
        item.setLowestOriginalPrice(lowestOriginalPrice);
        item.setReducePrice(Math.max(lowestOriginalPrice - lowestSellingPrice, 0));
        item.setIsCanBooking(isCanBooking);
        item.setTotalStock(String.valueOf(totalStock));
        item.setRoomCategoryType(row.getRoomCategoryType());
        item.setGoodsType(goodsTypeCode);
        item.setIsLongTermEffective(1);
        item.setEffectiveStartTime(row.getEffectiveStartTime());
        item.setEffectiveEndTime(row.getEffectiveEndTime());
        item.setPoiType(null);
        item.setParentPoiId(null);
        item.setPoiTags(null);
        item.setChannelPoiId(null);
        item.setPromotionDirectRatio(null);
        item.setEditionId(null);
        item.setEditionLevel(null);
        item.setEditionUpgradeType(null);
        item.setIsTransitionEdition(null);
        item.setIsFreeEdition(lowestSellingPrice == 0 ? 1 : 0);
        item.setGiftType(null);
        item.setOutRuleJson(null);
        item.setChannelOutRuleJson(null);
        item.setApplyId(row.getApplyId() == null ? String.valueOf(row.getGoodsId()) : row.getApplyId());
        item.setIsAvailability(String.valueOf(isCanBooking));
        item.setDescription(row.getDescription());
        item.setAttIds(null);
        item.setContractAgreementUrl(null);
        item.setBindRoomCategoryViews(null);
        item.setBindServiceViews(null);
        item.setRoomCategoryProductGetViews(safeProducts);
        return item;
    }

    private WeiRoomCategoryProductGetVO toProductVO(WeiRoomCategoryProductRowVO row) {
        WeiRoomCategoryProductGetVO product = new WeiRoomCategoryProductGetVO();
        product.setGoodsIdValue(row.getGoodsId());
        product.setRoomCategoryProductId(row.getRoomCategoryProductId());
        product.setRoomCategoryProductName(row.getRoomCategoryProductName());
        product.setSaleType(null);
        product.setStockType(null);
        product.setSellingPrice(row.getSellingPrice());
        product.setOriginalPrice(row.getOriginalPrice());
        product.setSettlementPrice(row.getSettlementPrice());
        product.setReducePrice(row.getOriginalPrice() == null || row.getSellingPrice() == null
                ? null
                : Math.max(row.getOriginalPrice() - row.getSellingPrice(), 0));
        product.setIsCanBooking(row.getIsCanBooking());
        product.setStock(row.getStock());
        product.setStockMode(row.getStockMode());
        product.setPhotoMediaId(row.getPhotoMediaId());
        product.setPhotoMediaUrl(row.getPhotoMediaUrl());
        product.setCancelPolicy(null);
        product.setBreakfastCount(null);
        product.setLunchCount(null);
        product.setDinnerCount(null);
        product.setCurDayBookingTime(null);
        product.setEarliestCheckInTime(null);
        product.setLatestCheckInTime(null);
        product.setLatestCheckOutTime(null);
        product.setHourCheckInTime(null);
        product.setHourCheckOutTime(null);
        product.setHourSerialCheckTime(null);
        product.setIsHourLimit(null);
        product.setSeq(row.getSeq());
        product.setExpandQuotaExpireTime(null);
        product.setBindRoomCategoryProductViews(List.of());
        return product;
    }

    private int deriveCanBooking(String shelfStatus, String stockMode, int totalStock) {
        boolean onShelf = "on_shelf".equalsIgnoreCase(defaultString(shelfStatus));
        boolean unlimited = "unlimited".equalsIgnoreCase(defaultString(stockMode));
        return onShelf && (unlimited || totalStock > 0) ? 1 : 0;
    }

    private boolean matchesGoodsTypes(int goodsTypeCode, List<Integer> goodsTypes) {
        return goodsTypes == null || goodsTypes.isEmpty() || goodsTypes.contains(goodsTypeCode);
    }

    private int resolveGoodsTypeCode(String rawGoodsType) {
        if (rawGoodsType == null || rawGoodsType.isBlank()) {
            return DEFAULT_GOODS_TYPE;
        }

        String trimmed = rawGoodsType.trim();
        if (trimmed.chars().allMatch(Character::isDigit)) {
            return Integer.parseInt(trimmed);
        }

        String normalized = trimmed.toLowerCase(Locale.ROOT);
        if (normalized.contains("edition") || normalized.contains("version")) {
            return VERSION_GOODS_TYPE;
        }
        if (normalized.contains("locals") || normalized.contains("mall") || normalized.contains("hardware")) {
            return LOCALS_MALL_GOODS_TYPE;
        }
        return DEFAULT_GOODS_TYPE;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private Long resolveAccessibleBuyCampId(Long requestedBuyCampId, Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "当前用户上下文不存在");
        }
        if (requestedBuyCampId == null) {
            return bundle.getCampId();
        }
        if (!requestedBuyCampId.equals(bundle.getCampId())) {
            throw new BusinessException(40301, "无权访问当前门店商品目录数据");
        }
        return requestedBuyCampId;
    }
}
