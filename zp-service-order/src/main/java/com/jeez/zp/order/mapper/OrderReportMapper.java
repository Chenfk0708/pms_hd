package com.jeez.zp.order.mapper;

import com.jeez.zp.order.vo.OrderReportRowVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderReportMapper {

    List<OrderReportRowVO> selectOrderReportRows(@Param("campId") Long campId);
}
