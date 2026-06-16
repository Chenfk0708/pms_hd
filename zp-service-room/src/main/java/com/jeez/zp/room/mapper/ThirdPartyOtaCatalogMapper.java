package com.jeez.zp.room.mapper;

import com.jeez.zp.room.vo.ThirdPartyOtaPoiVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ThirdPartyOtaCatalogMapper {

    long countPois(
            @Param("campId") Long campId,
            @Param("channelCode") String channelCode,
            @Param("keyword") String keyword
    );

    List<ThirdPartyOtaPoiVO> selectPois(
            @Param("campId") Long campId,
            @Param("channelCode") String channelCode,
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );
}
