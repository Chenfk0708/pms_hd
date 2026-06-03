package com.jeez.zp.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jeez.zp.platform.entity.SystemConfig;
import com.jeez.zp.platform.entity.UserSystemConfig;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.SystemConfigMapper;
import com.jeez.zp.platform.mapper.UserSystemConfigMapper;
import com.jeez.zp.platform.service.PlatformBootstrapService;
import com.jeez.zp.platform.service.SystemConfigService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.CommonCodeVO;
import com.jeez.zp.platform.vo.CommonsResponseVO;
import com.jeez.zp.platform.vo.SystemConfigItemVO;
import com.jeez.zp.platform.vo.SystemConfigsResponseVO;
import com.jeez.zp.platform.vo.UserShortcutResponseVO;
import com.jeez.zp.platform.vo.UserShortcutVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private static final String CHECK_WIFI_SHOW_KEY = "hudson.basic.checkWifiShowStrategy";
    private static final String DEFAULT_CHECK_IN_GUIDE_SHOW_VALUE = "0";
    private static final Map<String, Integer> DEFAULT_CHECK_WIFI_SHOW_VALUE = Map.of("isWifiDisplayEnabled", 0);
    private static final String NIGHT_AUDIT_ENABLED_KEY = "hudson.finance.isNightAudit";
    private static final String NIGHT_AUDIT_TIME_KEY = "hudson.finance.autoNightAuditTime";
    private static final String FINANCE_STRATEGY_KEY = "hudson.finance.orderAmortizeStrategy";
    private static final String VENDIBLE_TYPES_KEY = "hudson.finance.vendibleTypes";
    private static final String IM_SHORTCUTS_KEY = "hudson.im.userShortcuts";

    private final SystemConfigMapper systemConfigMapper;
    private final UserSystemConfigMapper userSystemConfigMapper;
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
    public SystemConfigItemVO getCheckWifiShowStrategy(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        SystemConfig existing = findCampConfig(resolvedCampId, CHECK_WIFI_SHOW_KEY);
        if (existing == null) {
            return buildConfigItem(CHECK_WIFI_SHOW_KEY, DEFAULT_CHECK_WIFI_SHOW_VALUE, CAMP_SCOPE, "system");
        }
        return buildConfigItem(existing.getConfigKey(), existing.getConfigValue(), existing.getConfigScope(), existing.getSource());
    }

    @Override
    @Transactional
    public SystemConfigsResponseVO updateCheckInGuideShowStrategy(
            Long campId,
            Long userId,
            Integer isCheckInGuideIdentityRegCompleted,
            Integer isCheckInGuideVerifyPayDeposit
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        Map<String, Integer> configValue = new LinkedHashMap<>();
        configValue.put(
                "isCheckInGuideIdentityRegCompleted",
                normalizeBinaryValue(isCheckInGuideIdentityRegCompleted, "isCheckInGuideIdentityRegCompleted")
        );
        configValue.put(
                "isCheckInGuideVerifyPayDeposit",
                normalizeBinaryValue(isCheckInGuideVerifyPayDeposit, "isCheckInGuideVerifyPayDeposit")
        );
        upsertCampConfig(resolvedCampId, userId, CHECK_IN_GUIDE_SHOW_KEY, configValue);
        return platformBootstrapService.getSystemConfigs(resolvedCampId, userId);
    }

    @Override
    @Transactional
    public SystemConfigsResponseVO updateCheckWifiShowStrategy(Long campId, Long userId, Integer isWifiDisplayEnabled) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        Map<String, Integer> configValue = new LinkedHashMap<>();
        configValue.put("isWifiDisplayEnabled", normalizeBinaryValue(isWifiDisplayEnabled, "isWifiDisplayEnabled"));
        upsertCampConfig(resolvedCampId, userId, CHECK_WIFI_SHOW_KEY, configValue);
        return platformBootstrapService.getSystemConfigs(resolvedCampId, userId);
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

    @Override
    @Transactional
    public SystemConfigsResponseVO updateNightAudit(Long campId, Long userId, Integer isNightAudit, Integer autoNightAuditTime) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        upsertCampConfig(resolvedCampId, userId, NIGHT_AUDIT_ENABLED_KEY, normalizeBinaryValue(isNightAudit, "isNightAudit"));
        upsertCampConfig(resolvedCampId, userId, NIGHT_AUDIT_TIME_KEY, normalizeNightAuditTime(autoNightAuditTime));
        return platformBootstrapService.getSystemConfigs(resolvedCampId, userId);
    }

    @Override
    @Transactional
    public SystemConfigsResponseVO updateFinanceStrategy(Long campId, Long userId, Integer orderAmortizeStrategy) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        if (orderAmortizeStrategy == null || (orderAmortizeStrategy != 1 && orderAmortizeStrategy != 2)) {
            throw new BusinessException(40002, "orderAmortizeStrategy must be 1 or 2");
        }
        upsertCampConfig(resolvedCampId, userId, FINANCE_STRATEGY_KEY, orderAmortizeStrategy);
        return platformBootstrapService.getSystemConfigs(resolvedCampId, userId);
    }

    @Override
    @Transactional
    public SystemConfigsResponseVO updateVendibleTypes(Long campId, Long userId, List<Integer> vendibleTypes) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        if (vendibleTypes == null || vendibleTypes.isEmpty()) {
            throw new BusinessException(40002, "vendibleTypes is required");
        }
        List<Integer> normalized = vendibleTypes.stream()
                .filter(value -> value != null && value >= 1 && value <= 5)
                .distinct()
                .toList();
        if (normalized.isEmpty()) {
            throw new BusinessException(40002, "vendibleTypes must contain values between 1 and 5");
        }
        upsertCampConfig(resolvedCampId, userId, VENDIBLE_TYPES_KEY, normalized);
        return platformBootstrapService.getSystemConfigs(resolvedCampId, userId);
    }


    @Override
    public CommonsResponseVO getCommons(Long campId, Long userId, String code) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(40001, "code is required");
        }
        SystemConfig existing = findCampConfig(resolvedCampId, code.trim());
        CommonsResponseVO response = new CommonsResponseVO();
        response.setCommons(existing == null ? List.of() : toCommons(existing.getConfigValue()));
        return response;
    }

    @Override
    public UserShortcutResponseVO getUserShortcuts(Long requestedUserId, Long currentUserId) {
        Long resolvedUserId = requestedUserId == null ? currentUserId : requestedUserId;
        if (!resolvedUserId.equals(currentUserId)) {
            throw new BusinessException(40301, "no permission to access another user's shortcuts");
        }
        CurrentUserBundleVO bundle = requireUserBundle(currentUserId);
        UserSystemConfig existing = findUserConfig(bundle.getCampId(), currentUserId, IM_SHORTCUTS_KEY);

        UserShortcutResponseVO response = new UserShortcutResponseVO();
        response.setUserShortcuts(existing == null ? List.of() : toUserShortcuts(existing.getConfigValue()));
        return response;
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
        upsertCampConfig(resolvedCampId, userId, expectedConfigKey, configValue.trim());
        return platformBootstrapService.getSystemConfigs(resolvedCampId, userId);
    }

    private void upsertCampConfig(Long campId, Long userId, String configKey, Object configValue) {
        SystemConfig existing = findCampConfig(campId, configKey);
        if (existing == null) {
            SystemConfig created = new SystemConfig();
            created.setSystemConfigId(IdWorker.getId());
            created.setCampId(campId);
            created.setConfigKey(configKey);
            created.setConfigScope(CAMP_SCOPE);
            created.setConfigValue(serializeConfigValue(configValue));
            created.setValueType(VALUE_TYPE_JSON);
            created.setSource(SOURCE_PLATFORM);
            created.setUpdatedBy(userId);
            systemConfigMapper.insert(created);
            return;
        }

        SystemConfig update = new SystemConfig();
        update.setSystemConfigId(existing.getSystemConfigId());
        update.setConfigValue(serializeConfigValue(configValue));
        update.setValueType(VALUE_TYPE_JSON);
        update.setSource(SOURCE_PLATFORM);
        update.setUpdatedBy(userId);
        systemConfigMapper.updateById(update);
    }

    private SystemConfig findCampConfig(Long campId, String configKey) {
        return systemConfigMapper.selectOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getCampId, campId)
                .eq(SystemConfig::getConfigScope, CAMP_SCOPE)
                .eq(SystemConfig::getConfigKey, configKey)
                .last("LIMIT 1"));
    }


    private UserSystemConfig findUserConfig(Long campId, Long userId, String configKey) {
        return userSystemConfigMapper.selectOne(new LambdaQueryWrapper<UserSystemConfig>()
                .eq(UserSystemConfig::getCampId, campId)
                .eq(UserSystemConfig::getUserId, userId)
                .eq(UserSystemConfig::getConfigKey, configKey)
                .last("LIMIT 1"));
    }

    private CurrentUserBundleVO requireUserBundle(Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "current user context not found");
        }
        return bundle;
    }

    private List<CommonCodeVO> toCommons(String rawValue) {
        Object parsed = parseJsonIfPossible(rawValue);
        if (!(parsed instanceof List<?> list)) {
            if (!StringUtils.hasText(String.valueOf(parsed))) {
                return List.of();
            }
            return List.of(new CommonCodeVO(String.valueOf(parsed)));
        }
        List<CommonCodeVO> commons = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Object codeName = map.get("codeName");
                if (codeName != null && StringUtils.hasText(String.valueOf(codeName))) {
                    commons.add(new CommonCodeVO(String.valueOf(codeName)));
                }
                continue;
            }
            if (item != null && StringUtils.hasText(String.valueOf(item))) {
                commons.add(new CommonCodeVO(String.valueOf(item)));
            }
        }
        return commons;
    }

    private List<UserShortcutVO> toUserShortcuts(String rawValue) {
        if (!StringUtils.hasText(rawValue)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(rawValue, new TypeReference<List<UserShortcutVO>>() {
            });
        } catch (JsonProcessingException ex) {
            throw new BusinessException(500, "failed to parse user shortcut config");
        }
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        CurrentUserBundleVO bundle = requireUserBundle(userId);
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!requestedCampId.equals(bundle.getCampId())) {
            throw new BusinessException(40301, "no permission to access current camp system config data");
        }
        return requestedCampId;
    }

    private void validateConfigKey(String expectedConfigKey, String actualConfigKey) {
        if (!expectedConfigKey.equals(actualConfigKey)) {
            throw new BusinessException(40003, "configKey does not match this endpoint");
        }
    }

    private void validateConfigValue(String configValue) {
        if (!StringUtils.hasText(configValue)) {
            throw new BusinessException(40002, "configValue is required");
        }
    }

    private int normalizeBinaryValue(Integer value, String fieldName) {
        if (value == null || (value != 0 && value != 1)) {
            throw new BusinessException(40002, fieldName + " must be 0 or 1");
        }
        return value;
    }

    private int normalizeNightAuditTime(Integer value) {
        if (value == null || value < 0 || value > 12) {
            throw new BusinessException(40002, "autoNightAuditTime must be between 0 and 12");
        }
        return value;
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

    private String serializeConfigValue(Object configValue) {
        try {
            return objectMapper.writeValueAsString(configValue);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(500, "failed to serialize configValue");
        }
    }
}
