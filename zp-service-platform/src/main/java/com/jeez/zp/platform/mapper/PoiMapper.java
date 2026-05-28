package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.PoiPageItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PoiMapper {

    long countPage(
            @Param("campId") Long campId,
            @Param("channelId") Long channelId,
            @Param("isAvailability") Integer isAvailability
    );

    List<PoiPageItemVO> selectPage(
            @Param("campId") Long campId,
            @Param("channelId") Long channelId,
            @Param("isAvailability") Integer isAvailability,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );
}
