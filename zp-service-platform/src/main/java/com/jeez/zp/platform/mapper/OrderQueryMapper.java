package com.jeez.zp.platform.mapper;

import com.jeez.zp.platform.vo.OrderQueryRowVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderQueryMapper {

    List<OrderQueryRowVO> selectHouseOrders(
            @Param("campId") Long campId,
            @Param("keyword") String keyword
    );
}
