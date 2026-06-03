package com.jeez.zp.crm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.jeez.zp.crm.dto.request.CustomerTagPageRequest;
import com.jeez.zp.crm.dto.request.CustomerTagSaveRequest;
import com.jeez.zp.crm.exception.BusinessException;
import com.jeez.zp.crm.mapper.CustomerTagMapper;
import com.jeez.zp.crm.service.CampAccessService;
import com.jeez.zp.crm.service.CustomerTagService;
import com.jeez.zp.crm.vo.CustomerTagExportResponseVO;
import com.jeez.zp.crm.vo.CustomerTagGroupQueryRowVO;
import com.jeez.zp.crm.vo.CustomerTagGroupVO;
import com.jeez.zp.crm.vo.CustomerTagPageResponseVO;
import com.jeez.zp.crm.vo.CustomerTagPaginationVO;
import com.jeez.zp.crm.vo.CustomerTagSaveResponseVO;
import com.jeez.zp.crm.vo.CustomerTagSummaryVO;
import com.jeez.zp.crm.vo.WeComAccountVO;
import com.jeez.zp.crm.vo.WeComAccountsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerTagServiceImpl implements CustomerTagService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int RECENT_DAYS = 7;

    private final CustomerTagMapper customerTagMapper;
    private final CampAccessService campAccessService;

    @Override
    public CustomerTagPageResponseVO getPage(CustomerTagPageRequest request, Long userId) {
        CustomerTagPageRequest safeRequest = request == null ? new CustomerTagPageRequest() : request;
        Long campId = campAccessService.resolveAccessibleCampId(parseLong(safeRequest.getCampId()), userId);
        int pageNum = normalizePageNum(safeRequest.getPage(), safeRequest.getPageNum(), safeRequest.getCurrent());
        int pageSize = safeRequest.getPageSize() == null || safeRequest.getPageSize() < 1 ? DEFAULT_PAGE_SIZE : safeRequest.getPageSize();

        List<CustomerTagGroupVO> rows = customerTagMapper.selectTagGroups(campId, resolveKeyword(safeRequest)).stream()
                .map(this::toGroup)
                .toList();

        int fromIndex = Math.min((pageNum - 1) * pageSize, rows.size());
        int toIndex = Math.min(fromIndex + pageSize, rows.size());
        List<CustomerTagGroupVO> pageRows = rows.subList(fromIndex, toIndex);

        CustomerTagPageResponseVO response = new CustomerTagPageResponseVO();
        response.setTotal((long) rows.size());
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setSummary(buildSummary(pageRows));
        response.setList(pageRows);
        response.setPagination(new CustomerTagPaginationVO(pageNum, pageSize, rows.size()));
        return response;
    }

    @Override
    @Transactional
    public CustomerTagSaveResponseVO save(CustomerTagSaveRequest request, Long userId) {
        if (request == null) {
            throw new BusinessException(40001, "request is required");
        }
        Long campId = campAccessService.resolveAccessibleCampId(parseLong(request.getCampId()), userId);
        Long tagGroupId = parseLong(request.getTagGroupId());
        if (tagGroupId == null) {
            tagGroupId = IdWorker.getId();
        }
        String tagGroupName = requireText(request.getTagGroupName(), "tagGroupName");
        List<String> tagNames = normalizeTagNames(request.getTagNames());
        customerTagMapper.upsertTagGroup(tagGroupId, campId, tagGroupName, normalizeSource(request.getSource()));

        for (int index = 0; index < tagNames.size(); index++) {
            customerTagMapper.upsertTag(IdWorker.getId(), tagGroupId, tagNames.get(index), index + 1);
        }

        CustomerTagSaveResponseVO response = new CustomerTagSaveResponseVO();
        response.setTagGroupId(String.valueOf(tagGroupId));
        response.setTagNames(tagNames);
        response.setMessage("customer tag group saved");
        return response;
    }

    @Override
    public CustomerTagExportResponseVO export(CustomerTagPageRequest request, Long userId) {
        CustomerTagPageRequest exportRequest = request == null ? new CustomerTagPageRequest() : request;
        exportRequest.setPageNum(DEFAULT_PAGE_NUM);
        exportRequest.setPageSize(Integer.MAX_VALUE);
        CustomerTagPageResponseVO page = getPage(exportRequest, userId);
        Long campId = campAccessService.resolveAccessibleCampId(parseLong(exportRequest.getCampId()), userId);

        CustomerTagExportResponseVO response = new CustomerTagExportResponseVO();
        response.setTaskId("MEMBER-TAG-GROUP-EXPORT-" + campId);
        response.setFileName("member_tag_groups_" + campId + ".csv");
        response.setContentType("text/csv");
        response.setTotal(page.getTotal());
        response.setRows(page.getList());
        return response;
    }

    @Override
    public WeComAccountsResponseVO getWeComAccounts(Long campId, Long userId) {
        Long resolvedCampId = campAccessService.resolveAccessibleCampId(campId, userId);
        List<WeComAccountVO> accounts = customerTagMapper.selectWeComAccounts(resolvedCampId);

        WeComAccountsResponseVO response = new WeComAccountsResponseVO();
        response.setTotal((long) accounts.size());
        response.setAccounts(accounts);
        return response;
    }

    private CustomerTagGroupVO toGroup(CustomerTagGroupQueryRowVO row) {
        CustomerTagGroupVO group = new CustomerTagGroupVO();
        group.setTagGroupId(row.getTagGroupId());
        group.setTagGroupName(row.getTagGroupName());
        group.setTagNames(splitTagNames(row.getTagNamesCsv()));
        group.setMemberCount(defaultInt(row.getMemberCount()));
        group.setRecentlyAddedCount(defaultInt(row.getRecentlyAddedCount()));
        group.setCreatedBy(row.getCreatedBy());
        group.setCreatedAt(row.getCreatedAt());
        group.setUpdatedAt(row.getUpdatedAt());
        group.setSource(row.getSource());
        group.setStatus(row.getStatus());
        group.setDescription(row.getDescription());
        return group;
    }

    private CustomerTagSummaryVO buildSummary(List<CustomerTagGroupVO> rows) {
        CustomerTagSummaryVO summary = new CustomerTagSummaryVO();
        summary.setGroupCount(rows.size());
        summary.setTagCount(rows.stream().map(CustomerTagGroupVO::getTagNames).mapToInt(List::size).sum());
        summary.setCoveredMembers(rows.stream().map(CustomerTagGroupVO::getMemberCount).mapToInt(this::defaultInt).sum());
        summary.setSyncingGroups((int) rows.stream().filter(row -> "syncing".equals(row.getStatus())).count());
        return summary;
    }

    private String resolveKeyword(CustomerTagPageRequest request) {
        String tagGroupName = trimToNull(request.getTagGroupName());
        return tagGroupName == null ? trimToNull(request.getKeyword()) : tagGroupName;
    }

    private List<String> normalizeTagNames(List<String> tagNames) {
        if (tagNames == null) {
            return List.of();
        }
        return tagNames.stream()
                .map(this::trimToNull)
                .filter(value -> value != null)
                .distinct()
                .toList();
    }

    private String normalizeSource(String source) {
        String normalized = trimToNull(source);
        return normalized == null ? "manual" : normalized;
    }

    private List<String> splitTagNames(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(this::trimToNull)
                .filter(item -> item != null)
                .toList();
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

    private String requireText(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BusinessException(40001, fieldName + " is required");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}
