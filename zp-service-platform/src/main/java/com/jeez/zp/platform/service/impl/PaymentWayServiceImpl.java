package com.jeez.zp.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeez.zp.platform.entity.PaymentWay;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PaymentWayMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.PaymentWayService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.PaymentWayVO;
import com.jeez.zp.platform.vo.PaymentWaysResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentWayServiceImpl implements PaymentWayService {

    private static final int SYSTEM_PAYMENT_WAY = 0;
    private static final int ENABLED = 1;

    private final PaymentWayMapper paymentWayMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public PaymentWaysResponseVO getPaymentWays(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<PaymentWayVO> paymentWays = paymentWayMapper.selectList(new LambdaQueryWrapper<PaymentWay>()
                        .eq(PaymentWay::getCampId, resolvedCampId)
                        .eq(PaymentWay::getIsDeleted, 0)
                        .eq(PaymentWay::getStatus, ENABLED)
                        .orderByAsc(PaymentWay::getSortNo, PaymentWay::getPaymentWayId))
                .stream()
                .map(this::toVO)
                .toList();

        PaymentWaysResponseVO response = new PaymentWaysResponseVO();
        response.setPaymentWays(paymentWays);
        return response;
    }

    private PaymentWayVO toVO(PaymentWay paymentWay) {
        PaymentWayVO vo = new PaymentWayVO();
        vo.setPaymentWayId(String.valueOf(paymentWay.getPaymentWayId()));
        vo.setPaymentWayName(paymentWay.getPaymentWayName());
        vo.setIsCustom(SYSTEM_PAYMENT_WAY);
        vo.setIsEnable(ENABLED);
        return vo;
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "褰撳墠鐢ㄦ埛涓婁笅鏂囦笉瀛樺湪");
        }
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!requestedCampId.equals(bundle.getCampId())) {
            throw new BusinessException(40301, "鏃犳潈璁块棶褰撳墠闂ㄥ簵鏀粯鏂瑰紡鏁版嵁");
        }
        return requestedCampId;
    }
}
