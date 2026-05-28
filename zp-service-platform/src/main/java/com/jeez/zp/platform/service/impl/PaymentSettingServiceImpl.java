package com.jeez.zp.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeez.zp.platform.entity.PaymentWay;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PaymentWayMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.PaymentSettingService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.PaymentMethodVO;
import com.jeez.zp.platform.vo.PaymentSettingListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentSettingServiceImpl implements PaymentSettingService {

    private static final int ENABLED = 1;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String NOTICE = "系统默认支付方式不支持编辑和删除，可直接拖动调整排序。";

    private final PaymentWayMapper paymentWayMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public PaymentSettingListVO getPaymentSettings(Long campId, Long userId, Boolean includeDisabled) {
        Long resolvedCampId = resolveListCampId(campId, userId);
        List<PaymentWay> paymentWays = paymentWayMapper.selectList(new LambdaQueryWrapper<PaymentWay>()
                .eq(PaymentWay::getCampId, resolvedCampId)
                .eq(PaymentWay::getIsDeleted, 0)
                .eq(Boolean.FALSE.equals(includeDisabled), PaymentWay::getStatus, ENABLED)
                .orderByDesc(PaymentWay::getStatus)
                .orderByAsc(PaymentWay::getSortNo, PaymentWay::getPaymentWayId));

        PaymentSettingListVO response = new PaymentSettingListVO();
        response.setMethods(paymentWays.stream().map(this::toVO).toList());
        response.setNotice(NOTICE);
        return response;
    }

    @Override
    public PaymentMethodVO getPaymentSettingDetail(Long methodId, Long userId) {
        CurrentUserBundleVO bundle = requiredUserBundle(userId);
        PaymentWay paymentWay = paymentWayMapper.selectOne(new LambdaQueryWrapper<PaymentWay>()
                .eq(PaymentWay::getPaymentWayId, methodId)
                .eq(PaymentWay::getCampId, bundle.getCampId())
                .eq(PaymentWay::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (paymentWay == null) {
            throw new BusinessException(40404, "支付方式不存在");
        }
        return toVO(paymentWay);
    }

    private PaymentMethodVO toVO(PaymentWay paymentWay) {
        PaymentMethodVO vo = new PaymentMethodVO();
        vo.setId(String.valueOf(paymentWay.getPaymentWayId()));
        vo.setCode(defaultString(paymentWay.getPaymentWayCode(), "payment_" + paymentWay.getPaymentWayId()));
        vo.setName(paymentWay.getPaymentWayName());
        vo.setStatus(isEnabled(paymentWay) ? "enabled" : "disabled");
        vo.setIsSystemDefault(true);
        vo.setIsPreferred(isEnabled(paymentWay) && isFirstSort(paymentWay));
        vo.setDescription(resolveDescription(paymentWay));
        vo.setAvailableScopes(resolveScopes(paymentWay.getWayType()));
        vo.setSettlementAccount(resolveSettlementAccount(paymentWay.getWayType()));
        vo.setLastUsedAt("未使用");
        vo.setUpdatedAt(paymentWay.getUpdatedAt() == null ? "" : paymentWay.getUpdatedAt().format(DATE_TIME_FORMATTER));
        vo.setRemark("系统支付方式：" + paymentWay.getPaymentWayName());
        vo.setUsageCountLabel(isEnabled(paymentWay) ? "近 30 天有收款记录" : "当前停用，暂无收款记录");
        return vo;
    }

    private Long resolveListCampId(Long requestedCampId, Long userId) {
        CurrentUserBundleVO bundle = requiredUserBundle(userId);
        if (requestedCampId == null || !requestedCampId.equals(bundle.getCampId())) {
            return bundle.getCampId();
        }
        return requestedCampId;
    }

    private CurrentUserBundleVO requiredUserBundle(Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "当前用户上下文不存在");
        }
        return bundle;
    }

    private boolean isEnabled(PaymentWay paymentWay) {
        return paymentWay.getStatus() != null && paymentWay.getStatus() == ENABLED;
    }

    private boolean isFirstSort(PaymentWay paymentWay) {
        return paymentWay.getSortNo() != null && paymentWay.getSortNo() == 1;
    }

    private String resolveDescription(PaymentWay paymentWay) {
        return switch (defaultString(paymentWay.getWayType(), "")) {
            case "online" -> "线上支付方式，适用于线上收款和扫码支付场景。";
            case "offline" -> "线下支付方式，适用于前台收款和人工核对场景。";
            case "platform" -> "平台统一代收方式，适用于统一清分和线上预付订单。";
            default -> "门店支付方式：" + paymentWay.getPaymentWayName();
        };
    }

    private List<String> resolveScopes(String wayType) {
        return switch (defaultString(wayType, "")) {
            case "online" -> List.of("线上收款", "扫码支付");
            case "offline" -> List.of("前台收款");
            case "platform" -> List.of("订单收款", "统一清分");
            default -> List.of("门店收款");
        };
    }

    private String resolveSettlementAccount(String wayType) {
        return switch (defaultString(wayType, "")) {
            case "online" -> "线上支付清分账户";
            case "offline" -> "门店现金账户";
            case "platform" -> "平台清分账户";
            default -> "待配置";
        };
    }

    private String defaultString(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
