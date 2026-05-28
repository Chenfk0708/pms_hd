package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.CategoryMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.CategoryService;
import com.jeez.zp.platform.vo.CategoriesResponseVO;
import com.jeez.zp.platform.vo.CategoryViewVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public CategoriesResponseVO getCategories(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<CategoryViewVO> categoryViews = categoryMapper.selectPresaleCategories(resolvedCampId).stream()
                .map(this::withEmptyChildren)
                .toList();

        CategoriesResponseVO response = new CategoriesResponseVO();
        response.setCategoryViews(categoryViews);
        return response;
    }

    private CategoryViewVO withEmptyChildren(CategoryViewVO categoryView) {
        categoryView.setChildren(List.of());
        return categoryView;
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
            throw new BusinessException(40301, "无权访问当前门店商品类目");
        }
        return requestedCampId;
    }
}
