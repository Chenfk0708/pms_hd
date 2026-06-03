package com.jeez.zp.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeez.zp.platform.entity.PaymentType;
import com.jeez.zp.platform.entity.PaymentTypeGroup;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PaymentTypeGroupMapper;
import com.jeez.zp.platform.mapper.PaymentTypeMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.PaymentTypeService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.PaymentTypeGroupVO;
import com.jeez.zp.platform.vo.PaymentTypeGroupsResponseVO;
import com.jeez.zp.platform.vo.PaymentTypeVO;
import com.jeez.zp.platform.vo.PaymentTypesResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentTypeServiceImpl implements PaymentTypeService {

    private final PaymentTypeMapper paymentTypeMapper;
    private final PaymentTypeGroupMapper paymentTypeGroupMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public PaymentTypesResponseVO getPaymentTypes(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<PaymentTypeVO> paymentTypes = paymentTypeMapper.selectList(new LambdaQueryWrapper<PaymentType>()
                        .eq(PaymentType::getCampId, resolvedCampId)
                        .eq(PaymentType::getIsDeleted, 0)
                        .orderByAsc(PaymentType::getSortNo, PaymentType::getPaymentTypeId))
                .stream()
                .map(this::toTypeVO)
                .toList();

        PaymentTypesResponseVO response = new PaymentTypesResponseVO();
        response.setPaymentTypes(paymentTypes);
        return response;
    }

    @Override
    public PaymentTypeGroupsResponseVO getPaymentTypesV2(Long campId, Long userId, List<Integer> bizTypes, Integer isEnable) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<PaymentType> paymentTypes = paymentTypeMapper.selectList(new LambdaQueryWrapper<PaymentType>()
                .eq(PaymentType::getCampId, resolvedCampId)
                .eq(PaymentType::getIsDeleted, 0)
                .in(bizTypes != null && !bizTypes.isEmpty(), PaymentType::getBizType, bizTypes)
                .eq(isEnable != null, PaymentType::getStatus, isEnable)
                .orderByAsc(PaymentType::getSortNo, PaymentType::getPaymentTypeId));

        PaymentTypeGroupsResponseVO response = new PaymentTypeGroupsResponseVO();
        if (paymentTypes.isEmpty()) {
            response.setPaymentGroups(List.of());
            return response;
        }

        List<Long> groupIds = paymentTypes.stream()
                .map(PaymentType::getPaymentTypeGroupId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<PaymentTypeGroup> groups = paymentTypeGroupMapper.selectList(new LambdaQueryWrapper<PaymentTypeGroup>()
                .eq(PaymentTypeGroup::getCampId, resolvedCampId)
                .eq(PaymentTypeGroup::getIsDeleted, 0)
                .in(!groupIds.isEmpty(), PaymentTypeGroup::getPaymentTypeGroupId, groupIds)
                .orderByAsc(PaymentTypeGroup::getSortNo, PaymentTypeGroup::getPaymentTypeGroupId));

        Map<Long, PaymentTypeGroup> groupMap = groups.stream()
                .collect(Collectors.toMap(PaymentTypeGroup::getPaymentTypeGroupId, group -> group, (left, right) -> left, LinkedHashMap::new));

        Map<Long, List<PaymentType>> paymentTypesByGroupId = paymentTypes.stream()
                .collect(Collectors.groupingBy(PaymentType::getPaymentTypeGroupId, LinkedHashMap::new, Collectors.toList()));

        List<PaymentTypeGroupVO> paymentGroups = new ArrayList<>();
        for (PaymentTypeGroup group : groups) {
            List<PaymentType> groupedPaymentTypes = paymentTypesByGroupId.remove(group.getPaymentTypeGroupId());
            if (groupedPaymentTypes == null || groupedPaymentTypes.isEmpty()) {
                continue;
            }
            paymentGroups.add(toGroupVO(group, groupedPaymentTypes));
        }

        for (Map.Entry<Long, List<PaymentType>> entry : paymentTypesByGroupId.entrySet()) {
            PaymentType fallback = entry.getValue().get(0);
            PaymentTypeGroup syntheticGroup = groupMap.get(entry.getKey());
            if (syntheticGroup == null) {
                syntheticGroup = new PaymentTypeGroup();
                syntheticGroup.setPaymentTypeGroupId(entry.getKey());
                syntheticGroup.setGroupType(fallback.getGroupType());
                syntheticGroup.setGroupName(fallback.getGroupName());
            }
            paymentGroups.add(toGroupVO(syntheticGroup, entry.getValue()));
        }

        response.setPaymentGroups(paymentGroups);
        return response;
    }

    private PaymentTypeGroupVO toGroupVO(PaymentTypeGroup group, List<PaymentType> paymentTypes) {
        PaymentTypeGroupVO groupVO = new PaymentTypeGroupVO();
        groupVO.setGroupType(group.getGroupType());
        groupVO.setGroupTypeName(group.getGroupName());
        groupVO.setPaymentTypes(paymentTypes.stream().map(this::toTypeVO).toList());
        return groupVO;
    }

    private PaymentTypeVO toTypeVO(PaymentType paymentType) {
        PaymentTypeVO typeVO = new PaymentTypeVO();
        typeVO.setPaymentTypeId(String.valueOf(paymentType.getPaymentTypeId()));
        typeVO.setPaymentTypeName(paymentType.getPaymentTypeName());
        typeVO.setIgnoreOrderGetItem(defaultInt(paymentType.getIgnoreOrderGetItem()));
        typeVO.setIsCustom(defaultInt(paymentType.getIsCustom()));
        typeVO.setIsIncome(defaultInt(paymentType.getIsIncome()));
        typeVO.setIsEnable(defaultInt(paymentType.getStatus()));
        typeVO.setBizType(paymentType.getBizType());
        typeVO.setGroupType(paymentType.getGroupType());
        return typeVO;
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "当前用户未绑定可用门店");
        }
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!requestedCampId.equals(bundle.getCampId())) {
            throw new BusinessException(40301, "无权访问当前门店支付方式");
        }
        return requestedCampId;
    }
}
