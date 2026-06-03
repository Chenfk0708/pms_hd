package com.jeez.zp.crm.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeez.zp.crm.dto.request.CampRequest;
import com.jeez.zp.crm.dto.request.ImPhrasePageRequest;
import com.jeez.zp.crm.exception.BusinessException;
import com.jeez.zp.crm.mapper.ImSettingMapper;
import com.jeez.zp.crm.service.CampAccessService;
import com.jeez.zp.crm.service.ImSettingService;
import com.jeez.zp.crm.vo.ImWordsGroupRowVO;
import com.jeez.zp.crm.vo.ImWordsGroupTreeResponseVO;
import com.jeez.zp.crm.vo.ImWordsGroupVO;
import com.jeez.zp.crm.vo.ImWordsPageResponseVO;
import com.jeez.zp.crm.vo.ImWordsRowVO;
import com.jeez.zp.crm.vo.ImYunxinUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImSettingServiceImpl implements ImSettingService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String YUNXIN_APP_KEY = "hudson.im.yunxin.appKey";

    private final ImSettingMapper imSettingMapper;
    private final CampAccessService campAccessService;
    private final ObjectMapper objectMapper;

    @Override
    public ImWordsGroupTreeResponseVO getPhraseGroupTree(CampRequest request, Long userId) {
        Long campId = campAccessService.resolveAccessibleCampId(parseLong(request == null ? null : request.getCampId()), userId);
        List<ImWordsGroupRowVO> rows = imSettingMapper.selectPhraseGroups(campId);
        Map<String, ImWordsGroupVO> byId = new LinkedHashMap<>();
        for (ImWordsGroupRowVO row : rows) {
            ImWordsGroupVO group = new ImWordsGroupVO();
            group.setImWordsGroupId(row.getImWordsGroupId());
            group.setName(row.getName());
            byId.put(row.getImWordsGroupId(), group);
        }

        List<ImWordsGroupVO> roots = new java.util.ArrayList<>();
        for (ImWordsGroupRowVO row : rows) {
            ImWordsGroupVO current = byId.get(row.getImWordsGroupId());
            ImWordsGroupVO parent = row.getParentGroupId() == null ? null : byId.get(row.getParentGroupId());
            if (parent == null) {
                roots.add(current);
            } else {
                parent.getChildren().add(current);
            }
        }

        ImWordsGroupTreeResponseVO response = new ImWordsGroupTreeResponseVO();
        response.setImWordsGroupGetViews(roots);
        return response;
    }

    @Override
    public ImWordsPageResponseVO getPhrasePage(ImPhrasePageRequest request, Long userId) {
        ImPhrasePageRequest safeRequest = request == null ? new ImPhrasePageRequest() : request;
        Long campId = campAccessService.resolveAccessibleCampId(parseLong(safeRequest.getCampId()), userId);
        int pageNum = normalizePageNum(safeRequest.getPage(), safeRequest.getPageNum(), safeRequest.getCurrent());
        int pageSize = safeRequest.getPageSize() == null || safeRequest.getPageSize() < 1 ? DEFAULT_PAGE_SIZE : safeRequest.getPageSize();
        List<ImWordsRowVO> rows = imSettingMapper.selectPhrases(
                campId,
                trimToNull(safeRequest.getKeyword()),
                parseLong(safeRequest.getImWordsGroupId())
        );

        int fromIndex = Math.min((pageNum - 1) * pageSize, rows.size());
        int toIndex = Math.min(fromIndex + pageSize, rows.size());

        ImWordsPageResponseVO response = new ImWordsPageResponseVO();
        response.setTotal((long) rows.size());
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setList(rows.subList(fromIndex, toIndex));
        return response;
    }

    @Override
    public ImYunxinUserVO getYunxinUser(CampRequest request, Long userId) {
        Long campId = campAccessService.resolveAccessibleCampId(parseLong(request == null ? null : request.getCampId()), userId);
        String rawAppKey = imSettingMapper.selectCampConfigValue(campId, YUNXIN_APP_KEY);
        String appKey = readConfigText(rawAppKey);
        if (!StringUtils.hasText(appKey)) {
            throw new BusinessException(40404, "yunxin appKey is not configured");
        }

        ImYunxinUserVO response = new ImYunxinUserVO();
        response.setAppKey(appKey);
        response.setAccid("pms_" + campId + "_" + userId);
        return response;
    }

    private String readConfigText(String rawValue) {
        if (!StringUtils.hasText(rawValue)) {
            return null;
        }
        String trimmed = rawValue.trim();
        try {
            Object parsed = objectMapper.readValue(trimmed, Object.class);
            return parsed instanceof String text ? text : objectMapper.writeValueAsString(parsed);
        } catch (JsonProcessingException ex) {
            return trimmed;
        }
    }

    private int normalizePageNum(Integer page, Integer pageNum, Integer current) {
        Integer candidate = page != null ? page : (pageNum != null ? pageNum : current);
        return candidate == null || candidate < 1 ? DEFAULT_PAGE_NUM : candidate;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
