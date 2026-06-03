package com.jeez.zp.crm.service.impl;

import com.jeez.zp.crm.dto.request.AuthorityExcludeRequest;
import com.jeez.zp.crm.mapper.AuthorityMapper;
import com.jeez.zp.crm.service.AuthorityService;
import com.jeez.zp.crm.service.CampAccessService;
import com.jeez.zp.crm.vo.AuthorityItemVO;
import com.jeez.zp.crm.vo.AuthorityModuleVO;
import com.jeez.zp.crm.vo.NotificationAuthorityResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorityServiceImpl implements AuthorityService {

    private final AuthorityMapper authorityMapper;
    private final CampAccessService campAccessService;

    @Override
    public NotificationAuthorityResponseVO getNotifications(Long campId, Long userId) {
        Long resolvedCampId = campAccessService.resolveAccessibleCampId(campId, userId);
        List<AuthorityItemVO> items = authorityMapper.selectNotificationAuthorities(resolvedCampId, userId);
        Map<String, List<AuthorityItemVO>> grouped = items.stream()
                .collect(Collectors.groupingBy(item -> resolveModuleName(item.getAuthorityCode())));

        NotificationAuthorityResponseVO response = new NotificationAuthorityResponseVO();
        response.setModules(grouped.entrySet().stream()
                .map(entry -> {
                    AuthorityModuleVO module = new AuthorityModuleVO();
                    module.setModuleName(entry.getKey());
                    module.setItems(entry.getValue());
                    return module;
                })
                .toList());
        return response;
    }

    @Override
    @Transactional
    public Boolean exclude(AuthorityExcludeRequest request, Long userId) {
        Long campId = campAccessService.resolveAccessibleCampId(parseLong(request.getCampId()), userId);
        for (Long authorityId : authorityIds(request)) {
            authorityMapper.upsertExclude(excludeId(campId, userId, authorityId), campId, userId, authorityId);
        }
        return true;
    }

    @Override
    @Transactional
    public Boolean restore(AuthorityExcludeRequest request, Long userId) {
        Long campId = campAccessService.resolveAccessibleCampId(parseLong(request.getCampId()), userId);
        for (Long authorityId : authorityIds(request)) {
            authorityMapper.restoreExclude(campId, userId, authorityId);
        }
        return true;
    }

    private List<Long> authorityIds(AuthorityExcludeRequest request) {
        if (request.getAuthorityIds() == null) {
            return List.of();
        }
        return request.getAuthorityIds().stream()
                .map(this::parseLong)
                .filter(value -> value != null)
                .toList();
    }

    private Long excludeId(Long campId, Long userId, Long authorityId) {
        return campId * 100000000 + userId * 1000 + authorityId % 1000;
    }

    private String resolveModuleName(String authorityCode) {
        if (authorityCode != null && authorityCode.startsWith("crm.")) {
            return "CRM通知";
        }
        return "通知";
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }
}
