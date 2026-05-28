package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.WeiRoomCategoryCatalogRowVO;
import com.jeez.zp.platform.vo.WeiRoomCategoryProductRowVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WeiRoomCategoryMapper {

    List<WeiRoomCategoryCatalogRowVO> selectCatalogRows(
            @Param("campId") Long campId,
            @Param("roomCategoryTypes") List<Integer> roomCategoryTypes,
            @Param("keyword") String keyword
    );

    List<WeiRoomCategoryProductRowVO> selectProductRows(@Param("goodsIds") List<Long> goodsIds);
}
