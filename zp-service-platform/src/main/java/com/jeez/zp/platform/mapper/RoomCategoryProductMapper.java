package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.RoomCategoryProductPageItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoomCategoryProductMapper {

    long countPage(
            @Param("campId") Long campId,
            @Param("keyword") String keyword,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("channelId") Long channelId
    );

    List<RoomCategoryProductPageItemVO> selectPage(
            @Param("campId") Long campId,
            @Param("keyword") String keyword,
            @Param("roomCategoryId") Long roomCategoryId,
            @Param("channelId") Long channelId,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );
}
