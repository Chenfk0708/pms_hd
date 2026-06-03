package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.EditionReplaceOrderQueryRowVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EditionReplaceOrderMapper {

    List<EditionReplaceOrderQueryRowVO> selectReplaceOrderRows(
            @Param("campId") Long campId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEndExclusive") LocalDateTime rangeEndExclusive
    );
}
