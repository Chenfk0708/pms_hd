package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.CommodityDetailRowVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommodityMapper {

    CommodityDetailRowVO selectCommodityDetail(
            @Param("catalogCampId") Long catalogCampId,
            @Param("goodsId") Long goodsId
    );

    List<Long> selectRoomCategoryIds(@Param("goodsId") Long goodsId);
}
