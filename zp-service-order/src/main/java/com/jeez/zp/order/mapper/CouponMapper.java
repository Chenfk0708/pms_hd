package com.jeez.zp.order.mapper;

import com.jeez.zp.order.vo.CouponRowVO;
import com.jeez.zp.order.vo.CouponSendConfigRowVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CouponMapper {

    List<CouponRowVO> selectCoupons(@Param("campId") Long campId, @Param("shelfStatus") Integer shelfStatus);

    List<CouponSendConfigRowVO> selectCouponSendConfigs(@Param("campId") Long campId);
}
