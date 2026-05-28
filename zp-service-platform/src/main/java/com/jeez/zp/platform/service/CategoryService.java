package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.CategoriesResponseVO;

public interface CategoryService {

    CategoriesResponseVO getCategories(Long campId, Long userId);
}
