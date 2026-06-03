package com.jeez.zp.room.mapper;

import com.jeez.zp.room.vo.PriceLogOptionVO;
import com.jeez.zp.room.vo.PriceLogRowVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PriceLogMapper {

    List<PriceLogRowVO> selectRows(
            @Param("campId") Long campId,
            @Param("keyword") String keyword,
            @Param("channelId") Long channelId,
            @Param("adjustmentStart") LocalDate adjustmentStart,
            @Param("adjustmentEnd") LocalDate adjustmentEnd,
            @Param("operationStart") LocalDateTime operationStart,
            @Param("operationEndExclusive") LocalDateTime operationEndExclusive
    );

    List<PriceLogOptionVO> selectChannelOptions(@Param("campId") Long campId);
}
