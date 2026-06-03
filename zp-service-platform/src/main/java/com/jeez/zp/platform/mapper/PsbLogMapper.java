package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.PsbLogRowVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PsbLogMapper {

    long countPage(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("keyword") String keyword,
            @Param("bizType") String bizType,
            @Param("state") String state
    );

    List<PsbLogRowVO> selectPage(
            @Param("campId") Long campId,
            @Param("poiId") Long poiId,
            @Param("keyword") String keyword,
            @Param("bizType") String bizType,
            @Param("state") String state,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );

    PsbLogRowVO selectByGuestIdAndOrderNo(
            @Param("campId") Long campId,
            @Param("guestId") Long guestId,
            @Param("orderNo") String orderNo
    );
}
