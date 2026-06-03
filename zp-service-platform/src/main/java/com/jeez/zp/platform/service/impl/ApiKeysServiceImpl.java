package com.jeez.zp.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeez.zp.platform.entity.CompanyApiKey;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.CompanyApiKeyMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.ApiKeysService;
import com.jeez.zp.platform.vo.ApiKeyRecordVO;
import com.jeez.zp.platform.vo.ApiKeysActivityVO;
import com.jeez.zp.platform.vo.ApiKeysPayloadVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiKeysServiceImpl implements ApiKeysService {

    private static final int ACTIVE_STATUS = 1;
    private static final int INACTIVE_STATUS = 0;
    private static final int NOT_DELETED = 0;
    private static final int DELETED = 1;
    private static final String ACTIVE_STATUS_TEXT = "active";
    private static final String NEVER_USED_TEXT = "尚未使用";
    private static final String ROTATION_TIP = "建议在 90 天内完成轮换";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter ACCESS_KEY_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final List<String> DEFAULT_SCOPES = List.of(
            "Locals AI 服务端接入",
            "推理调用鉴权",
            "环境隔离密钥托管"
    );

    private final CompanyApiKeyMapper companyApiKeyMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public ApiKeysPayloadVO get(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        return buildPayload(selectActiveKey(resolvedCampId));
    }

    @Override
    @Transactional
    public ApiKeysPayloadVO generate(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        LocalDateTime now = LocalDateTime.now();
        rotateActiveKeys(resolvedCampId, now);

        CompanyApiKey apiKey = new CompanyApiKey();
        apiKey.setApiKeyId(IdWorker.getId());
        apiKey.setCampId(resolvedCampId);
        apiKey.setAppId(buildAppId(resolvedCampId, now));
        apiKey.setAccessKeyId(buildAccessKeyId(now));
        apiKey.setStatus(ACTIVE_STATUS);
        apiKey.setIsDeleted(NOT_DELETED);
        apiKey.setScopesJson(serializeScopes(DEFAULT_SCOPES));
        apiKey.setCreatedAt(now);
        apiKey.setUpdatedAt(now);

        String secret = buildSecret(now);
        apiKey.setSecretKeyCiphertext(hashSecret(secret));
        apiKey.setSecretKeyPreview(buildSecretPreview(secret, now));

        companyApiKeyMapper.insert(apiKey);
        return buildPayload(apiKey);
    }

    private CompanyApiKey selectActiveKey(Long campId) {
        return companyApiKeyMapper.selectOne(new LambdaQueryWrapper<CompanyApiKey>()
                .eq(CompanyApiKey::getCampId, campId)
                .eq(CompanyApiKey::getStatus, ACTIVE_STATUS)
                .eq(CompanyApiKey::getIsDeleted, NOT_DELETED)
                .orderByDesc(CompanyApiKey::getCreatedAt)
                .orderByDesc(CompanyApiKey::getApiKeyId)
                .last("LIMIT 1"));
    }

    private void rotateActiveKeys(Long campId, LocalDateTime now) {
        companyApiKeyMapper.update(null, new LambdaUpdateWrapper<CompanyApiKey>()
                .eq(CompanyApiKey::getCampId, campId)
                .eq(CompanyApiKey::getStatus, ACTIVE_STATUS)
                .eq(CompanyApiKey::getIsDeleted, NOT_DELETED)
                .set(CompanyApiKey::getStatus, INACTIVE_STATUS)
                .set(CompanyApiKey::getIsDeleted, DELETED)
                .set(CompanyApiKey::getRotatedAt, now)
                .set(CompanyApiKey::getUpdatedAt, now));
    }

    private ApiKeysPayloadVO buildPayload(CompanyApiKey apiKey) {
        ApiKeysPayloadVO payload = new ApiKeysPayloadVO();
        if (apiKey == null) {
            payload.setKeyRecord(null);
            payload.setActivityLog(List.of());
            return payload;
        }

        payload.setKeyRecord(toKeyRecord(apiKey));
        payload.setActivityLog(List.of(buildActivity(apiKey)));
        return payload;
    }

    private ApiKeyRecordVO toKeyRecord(CompanyApiKey apiKey) {
        ApiKeyRecordVO record = new ApiKeyRecordVO();
        record.setAppId(apiKey.getAppId());
        record.setAccessKeyId(apiKey.getAccessKeyId());
        record.setSecretKeyPreview(apiKey.getSecretKeyPreview());
        record.setCreatedAt(formatTime(apiKey.getCreatedAt()));
        record.setLastUsedAt(apiKey.getLastUsedAt() == null ? NEVER_USED_TEXT : formatTime(apiKey.getLastUsedAt()));
        record.setRotationTip(ROTATION_TIP);
        record.setStatus(ACTIVE_STATUS_TEXT);
        record.setScopes(parseScopes(apiKey.getScopesJson()));
        return record;
    }

    private ApiKeysActivityVO buildActivity(CompanyApiKey apiKey) {
        ApiKeysActivityVO activity = new ApiKeysActivityVO();
        activity.setId(String.valueOf(apiKey.getApiKeyId()));
        activity.setTitle("API Key 已就绪");
        activity.setDetail("当前凭证状态正常，可用于 Locals AI 服务端接入。");
        activity.setOccurredAt(formatTime(apiKey.getCreatedAt()));
        return activity;
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
            throw new BusinessException(40301, "无权访问当前租户 API Keys");
        }
        return requestedCampId;
    }

    private String buildAppId(Long campId, LocalDateTime now) {
        return "locals-ai-" + campId + "-" + now.format(DATE_FORMATTER) + randomHex(2);
    }

    private String buildAccessKeyId(LocalDateTime now) {
        return "ak_local_" + now.format(ACCESS_KEY_TIME_FORMATTER) + "_" + randomHex(6);
    }

    private String buildSecret(LocalDateTime now) {
        return "sk_local_" + now.format(ACCESS_KEY_TIME_FORMATTER) + "_" + randomHex(32);
    }

    private String buildSecretPreview(String secret, LocalDateTime now) {
        String prefix = secret.substring(0, Math.min(secret.length(), 26));
        return prefix + "_****************";
    }

    private String hashSecret(String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return "sha256:" + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new BusinessException(500, "API Key 摘要算法不可用");
        }
    }

    private String serializeScopes(List<String> scopes) {
        try {
            return objectMapper.writeValueAsString(scopes);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(500, "API Key 权限范围序列化失败");
        }
    }

    private List<String> parseScopes(String scopesJson) {
        if (!StringUtils.hasText(scopesJson)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(scopesJson, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            throw new BusinessException(500, "API Key 权限范围解析失败");
        }
    }

    private String randomHex(int byteLength) {
        byte[] buffer = new byte[byteLength];
        secureRandom.nextBytes(buffer);
        return HexFormat.of().formatHex(buffer);
    }

    private String formatTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DISPLAY_TIME_FORMATTER);
    }
}
