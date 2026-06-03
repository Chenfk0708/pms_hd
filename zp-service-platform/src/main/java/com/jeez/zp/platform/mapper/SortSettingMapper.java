package com.jeez.zp.platform.mapper;

import org.apache.ibatis.annotations.Param;

public interface SortSettingMapper {

    int updateRoomCategorySortNo(
            @Param("campId") Long campId,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("sortNo") Integer sortNo,
            @Param("userId") Long userId
    );

    int updateGoodsSortRemark(
            @Param("campId") Long campId,
            @Param("goodsId") Long goodsId,
            @Param("sortNo") Integer sortNo,
            @Param("userId") Long userId
    );
}
