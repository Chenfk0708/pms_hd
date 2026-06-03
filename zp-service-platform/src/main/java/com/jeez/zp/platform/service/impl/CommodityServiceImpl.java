package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.CommodityMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.CommodityService;
import com.jeez.zp.platform.vo.CommodityDetailResponseVO;
import com.jeez.zp.platform.vo.CommodityDetailRowVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommodityServiceImpl implements CommodityService {

    private static final long DEFAULT_CATALOG_CAMP_ID = 64L;

    private final CommodityMapper commodityMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public CommodityDetailResponseVO getCommodityDetail(Long campId, Long commodityId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        if (commodityId == null) {
            throw new BusinessException(40001, "商品ID不能为空");
        }

        CommodityDetailRowVO row = commodityMapper.selectCommodityDetail(DEFAULT_CATALOG_CAMP_ID, commodityId);
        if (row == null) {
            throw new BusinessException(40404, "商品不存在");
        }

        CommodityDetailResponseVO response = new CommodityDetailResponseVO();
        response.setCommodityId(String.valueOf(row.getGoodsId()));
        response.setCommodityName(row.getCommodityName());
        response.setDescription(defaultString(row.getDescription()));
        response.setMainPhoto(defaultString(row.getMainPhoto()));
        response.setSellingPriceCent(defaultLong(row.getSellingPriceCent()));
        response.setOriginalPriceCent(defaultLong(row.getOriginalPriceCent()));
        response.setSettlementPriceCent(defaultLong(row.getSettlementPriceCent()));
        response.setPurchaseTermLabel(defaultString(row.getPurchaseTermLabel()));
        response.setRoomCategoryIds(commodityMapper.selectRoomCategoryIds(commodityId).stream().map(String::valueOf).toList());
        return response;
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
            throw new BusinessException(40301, "无权访问当前门店商品详情");
        }
        return requestedCampId;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
