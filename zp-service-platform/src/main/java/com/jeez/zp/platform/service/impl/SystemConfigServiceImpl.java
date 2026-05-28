package com.jeez.zp.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeez.zp.platform.entity.SystemConfig;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.SystemConfigMapper;
import com.jeez.zp.platform.service.PlatformBootstrapService;
import com.jeez.zp.platform.service.SystemConfigService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.SystemConfigItemVO;
import com.jeez.zp.platform.vo.SystemConfigsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private static final String CAMP_SCOPE = "camp";
    private static final String VALUE_TYPE_JSON = "json";
    private static final String SOURCE_PLATFORM = "platform";
    private static final String ORDER_AUTO_PENDING_KEY = "hudson.basic.orderAutoPendingStrategy";
    private static final String ORDER_AUTO_SETTLE_KEY = "hudson.basic.orderAutoSettleStrategy";
    private static final String NEGOTIATE_REFUND_KEY = "hudson.basic.negotiateRefundAutomaticAcceptStrategy";
    private static final String CHECK_IN_GUIDE_SHOW_KEY = "hudson.basic.checkInGuideShowStrategy";
    private static final String DEFAULT_CHECK_IN_GUIDE_SHOW_VALUE = "0";

    private final SystemConfigMapper systemConfigMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;
    private final PlatformBootstrapService platformBootstrapService;
    private final ObjectMapper objectMapper;

    @Override
    public SystemConfigItemVO getCheckInGuideShowStrategy(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        SystemConfig existing = findCampConfig(resolvedCampId, CHECK_IN_GUIDE_SHOW_KEY);
        if (existing == null) {
            return buildConfigItem(CHECK_IN_GUIDE_SHOW_KEY, DEFAULT_CHECK_IN_GUIDE_SHOW_VALUE, CAMP_SCOPE, "system");
        }
        return buildConfigItem(existing.getConfigKey(), existing.getConfigValue(), existing.getConfigScope(), existing.getSource());
    }

    @Override
    @Transactional
    public SystemConfigsResponseVO updateOrderAutoPendingStrategy(Long campId, Long userId, String configKey, String configValue) {
        return saveCampConfig(campId, userId, ORDER_AUTO_PENDING_KEY, configKey, configValue);
    }

    @Override
    @Transactional
    public SystemConfigsResponseVO updateOrderAutoSettleStrategy(Long campId, Long userId, String configKey, String configValue) {
        return saveCampConfig(campId, userId, ORDER_AUTO_SETTLE_KEY, configKey, configValue);
    }

    @Override
    @Transactional
    public SystemConfigsResponseVO updateNegotiateRefundAutomaticAcceptStrategy(Long campId, Long userId, String configKey, String configValue) {
        return saveCampConfig(campId, userId, NEGOTIATE_REFUND_KEY, configKey, configValue);
    }

    private SystemConfigsResponseVO saveCampConfig(
            Long requestedCampId,
            Long userId,
            String expectedConfigKey,
            String actualConfigKey,
            String configValue
    ) {
        Long resolvedCampId = resolveAccessibleCampId(requestedCampId, userId);
        validateConfigKey(expectedConfigKey, actualConfigKey);
        validateConfigValue(configValue);

        SystemConfig existing = findCampConfig(resolvedCampId, expectedConfigKey);

        if (existing == null) {
            SystemConfig created = new SystemConfig();
            created.setSystemConfigId(IdWorker.getId());
            created.setCampId(resolvedCampId);
            created.setConfigKey(expectedConfigKey);
            created.setConfigScope(CAMP_SCOPE);
            created.setConfigValue(serializeConfigValue(configValue.trim()));
            created.setValueType(VALUE_TYPE_JSON);
            created.setSource(SOURCE_PLATFORM);
            created.setUpdatedBy(userId);
            systemConfigMapper.insert(created);
        } else {
            SystemConfig update = new SystemConfig();
            update.setSystemConfigId(existing.getSystemConfigId());
            update.setConfigValue(serializeConfigValue(configValue.trim()));
            update.setValueType(VALUE_TYPE_JSON);
            update.setSource(SOURCE_PLATFORM);
            update.setUpdatedBy(userId);
            systemConfigMapper.updateById(update);
        }

        return platformBootstrapService.getSystemConfigs(resolvedCampId, userId);
    }

    private SystemConfig findCampConfig(Long campId, String configKey) {
        return systemConfigMapper.selectOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getCampId, campId)
                .eq(SystemConfig::getConfigScope, CAMP_SCOPE)
                .eq(SystemConfig::getConfigKey, configKey)
                .last("LIMIT 1"));
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "当前用户上下文不存在");
        }
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!requestedCampId.equals(bundle.getCampId())) {
            throw new BusinessException(40301, "无权访问当前租户系统配置数据");
        }
        return requestedCampId;
    }

    private void validateConfigKey(String expectedConfigKey, String actualConfigKey) {
        if (!expectedConfigKey.equals(actualConfigKey)) {
            throw new BusinessException(40003, "配置键与当前接口不匹配");
        }
    }

    private void validateConfigValue(String configValue) {
        if (!StringUtils.hasText(configValue)) {
            throw new BusinessException(40002, "配置值不能为空");
        }
    }

    private SystemConfigItemVO buildConfigItem(String configKey, Object rawConfigValue, String configScope, String source) {
        SystemConfigItemVO item = new SystemConfigItemVO();
        item.setConfigKey(configKey);
        item.setConfigValue(parseJsonIfPossible(rawConfigValue));
        item.setConfigScope(configScope);
        item.setSource(StringUtils.hasText(source) ? source : "system");
        return item;
    }

    private Object parseJsonIfPossible(Object rawValue) {
        if (!(rawValue instanceof String textValue)) {
            return rawValue;
        }
        try {
            return objectMapper.readValue(textValue, Object.class);
        } catch (Exception ignored) {
            return textValue;
        }
    }

    private String serializeConfigValue(String configValue) {
        try {
            return objectMapper.writeValueAsString(configValue);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(500, "配置值序列化失败");
        }
    }
}
