package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.CategoryViewVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CategoryMapper {

    List<CategoryViewVO> selectPresaleCategories(@Param("campId") Long campId);
}
