package com.jeez.zp.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeez.common.utils.InputValidationUtils;
import com.jeez.zp.platform.dto.request.MemberSettingDraftRequest;
import com.jeez.zp.platform.dto.request.MemberSettingsBootstrapRequest;
import com.jeez.zp.platform.dto.request.MemberSettingsSaveRequest;
import com.jeez.zp.platform.dto.request.MemberWecomBindRequest;
import com.jeez.zp.platform.entity.PmsMember;
import com.jeez.zp.platform.entity.PmsRole;
import com.jeez.zp.platform.entity.PmsUser;
import com.jeez.zp.platform.entity.UserSystemConfig;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.MemberSettingMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.PmsMemberMapper;
import com.jeez.zp.platform.mapper.PmsRoleMapper;
import com.jeez.zp.platform.mapper.PmsUserMapper;
import com.jeez.zp.platform.mapper.UserSystemConfigMapper;
import com.jeez.zp.platform.service.MemberSettingService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.MemberSettingDraftVO;
import com.jeez.zp.platform.vo.MemberSettingEditorVO;
import com.jeez.zp.platform.vo.MemberSettingMemberVO;
import com.jeez.zp.platform.vo.MemberSettingPaginationVO;
import com.jeez.zp.platform.vo.MemberSettingRoleVO;
import com.jeez.zp.platform.vo.MemberSettingRoomCategoryVO;
import com.jeez.zp.platform.vo.MemberSettingSummaryVO;
import com.jeez.zp.platform.vo.MemberSettingsResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberSettingServiceImpl implements MemberSettingService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int EMPLOYEE_LIMIT = 3;
    private static final int ACTIVE_STATUS = 1;
    private static final int NOT_DELETED = 0;
    private static final String ROLE_ALL_NAME = "全部";
    private static final String MEMBER_ROOM_CATEGORY_CONFIG_PREFIX = "member.roomCategoryIds.";

    private final MemberSettingMapper memberSettingMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;
    private final PmsUserMapper pmsUserMapper;
    private final PmsMemberMapper pmsMemberMapper;
    private final PmsRoleMapper pmsRoleMapper;
    private final UserSystemConfigMapper userSystemConfigMapper;
    private final ObjectMapper objectMapper;

    @Override
    public MemberSettingsResponseVO bootstrap(MemberSettingsBootstrapRequest request, Long userId) {
        Long campId = resolveAccessibleCampId(parseLong(request.getCampId()), userId);
        String routeMode = normalizeRouteMode(request.getRouteMode());
        String roleName = normalizeRoleNameFilter(request.getRoleName());
        int page = normalizePage(request.getPage());
        int pageSize = normalizePageSize(request.getPageSize());
        List<MemberSettingMemberVO> allMembers = memberSettingMapper.selectMembers(campId, normalizeKeyword(request.getKeyword()), roleName);
        fillMemberRoomCategoryIds(campId, allMembers);

        int total = allMembers.size();
        int fromIndex = Math.min((page - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<MemberSettingMemberVO> pageMembers = new ArrayList<>(allMembers.subList(fromIndex, toIndex));

        MemberSettingsResponseVO response = new MemberSettingsResponseVO();
        response.setSummary(new MemberSettingSummaryVO(total, EMPLOYEE_LIMIT));
        response.setRoles(buildRoleOptions(campId));
        response.setMembers(pageMembers);
        response.setPendingFlows(List.of());
        response.setRoomCategories(loadRoomCategories(campId));
        response.setPagination(new MemberSettingPaginationVO(page, pageSize, total));
        response.setEditor(buildEditor(campId, routeMode, request.getEditUserId()));
        return response;
    }

    @Override
    @Transactional
    public List<MemberSettingMemberVO> save(MemberSettingsSaveRequest request, Long userId) {
        Long campId = resolveAccessibleCampId(parseLong(request.getCampId()), userId);
        MemberSettingDraftRequest draft = request.getDraft();
        validateDraft(campId, draft);
        Long roleId = parseRequiredLong(draft.getRoleId(), "roleId");
        PmsRole role = loadRole(campId, roleId);
        Long savedUserId = StringUtils.hasText(draft.getUserId())
                ? updateMember(campId, userId, draft, role)
                : createMember(campId, userId, draft, role);
        saveMemberRoomCategories(campId, savedUserId, draft.getRoomCategoryIds());
        List<MemberSettingMemberVO> members = memberSettingMapper.selectMembers(campId, normalizePhone(draft.getPhone()), null);
        fillMemberRoomCategoryIds(campId, members);
        return members;
    }

    @Override
    @Transactional
    public List<MemberSettingMemberVO> bindWecom(MemberWecomBindRequest request, Long userId) {
        Long campId = resolveAccessibleCampId(parseLong(request.getCampId()), userId);
        Long targetUserId = parseFlexibleUserId(request.getUserId());
        PmsMember member = loadMemberByUserOrMobile(campId, request.getUserId(), targetUserId);
        PmsMember update = new PmsMember();
        update.setMemberId(member.getMemberId());
        update.setWecomUserId("wecom-" + member.getMemberId());
        update.setUpdatedBy(userId);
        pmsMemberMapper.updateById(update);

        List<MemberSettingMemberVO> members = memberSettingMapper.selectMembers(campId, member.getMobile(), null);
        fillMemberRoomCategoryIds(campId, members);
        return members;
    }

    private Long createMember(Long campId, Long operatorUserId, MemberSettingDraftRequest draft, PmsRole role) {
        Long userId = IdWorker.getId();
        String phone = normalizePhone(draft.getPhone());
        String name = normalizeRequiredText(draft.getName(), "name");

        PmsUser user = new PmsUser();
        user.setUserId(userId);
        user.setMobile(phone);
        user.setNickName(name);
        user.setStatus(ACTIVE_STATUS);
        user.setIsDeleted(NOT_DELETED);
        pmsUserMapper.insert(user);

        PmsMember member = new PmsMember();
        member.setMemberId(IdWorker.getId());
        member.setCampId(campId);
        member.setRoleId(role.getRoleId());
        member.setUserId(userId);
        member.setName(name);
        member.setMobile(phone);
        member.setEmail("-");
        member.setStatus(ACTIVE_STATUS);
        member.setCreatedBy(operatorUserId);
        member.setUpdatedBy(operatorUserId);
        member.setIsDeleted(NOT_DELETED);
        member.setVersionNo(0);
        pmsMemberMapper.insert(member);
        return userId;
    }

    private Long updateMember(Long campId, Long operatorUserId, MemberSettingDraftRequest draft, PmsRole role) {
        PmsMember member = loadMemberByUserOrMobile(campId, draft.getUserId(), parseFlexibleUserId(draft.getUserId()));
        String phone = normalizePhone(draft.getPhone());
        String name = normalizeRequiredText(draft.getName(), "name");

        if (member.getUserId() != null) {
            PmsUser user = new PmsUser();
            user.setUserId(member.getUserId());
            user.setMobile(phone);
            user.setNickName(name);
            pmsUserMapper.updateById(user);
        }

        PmsMember update = new PmsMember();
        update.setMemberId(member.getMemberId());
        update.setRoleId(role.getRoleId());
        update.setName(name);
        update.setMobile(phone);
        update.setUpdatedBy(operatorUserId);
        pmsMemberMapper.updateById(update);
        return member.getUserId() == null ? member.getMemberId() : member.getUserId();
    }

    private MemberSettingEditorVO buildEditor(Long campId, String routeMode, String editUserId) {
        boolean editMode = "edit".equals(routeMode);
        MemberSettingEditorVO editor = new MemberSettingEditorVO();
        editor.setTitle(editMode ? "编辑成员" : "添加成员");
        editor.setSubmitText(editMode ? "保存" : "提交");
        editor.setBreadcrumbText("成员设置 / " + editor.getTitle());
        editor.setRolePlaceholder("请选择角色");
        editor.setRoomSearchPlaceholder("搜索房型名称");
        editor.setDraft(editMode ? buildEditDraft(campId, editUserId) : buildEmptyDraft());
        return editor;
    }

    private MemberSettingDraftVO buildEditDraft(Long campId, String editUserId) {
        Long userId = parseFlexibleUserId(editUserId);
        MemberSettingMemberVO member = memberSettingMapper.selectMemberByUserId(campId, userId);
        if (member == null) {
            return buildEmptyDraft();
        }
        fillMemberRoomCategoryIds(campId, List.of(member));

        MemberSettingDraftVO draft = new MemberSettingDraftVO();
        draft.setUserId(member.getUserId());
        draft.setName(member.getName());
        draft.setPhone(member.getPhone());
        draft.setRoleId(member.getRoleId());
        draft.setRoleName(member.getRoleName());
        draft.setRoomCategoryIds(member.getRoomCategoryIds());
        return draft;
    }

    private MemberSettingDraftVO buildEmptyDraft() {
        MemberSettingDraftVO draft = new MemberSettingDraftVO();
        draft.setName("");
        draft.setPhone("");
        draft.setRoleId("");
        draft.setRoleName("");
        draft.setRoomCategoryIds(List.of());
        return draft;
    }

    private List<MemberSettingRoleVO> buildRoleOptions(Long campId) {
        MemberSettingRoleVO all = new MemberSettingRoleVO();
        all.setRoleId("all");
        all.setRoleName(ROLE_ALL_NAME);

        List<MemberSettingRoleVO> roles = new ArrayList<>();
        roles.add(all);
        roles.addAll(memberSettingMapper.selectRoles(campId));
        return roles;
    }

    private List<MemberSettingRoomCategoryVO> loadRoomCategories(Long campId) {
        List<MemberSettingRoomCategoryVO> roomCategories = memberSettingMapper.selectRoomCategories(campId);
        roomCategories.forEach(item -> item.setRoomIds(List.of()));
        return roomCategories;
    }

    private void fillMemberRoomCategoryIds(Long campId, List<MemberSettingMemberVO> members) {
        if (members.isEmpty()) {
            return;
        }
        List<String> defaultIds = loadRoomCategories(campId).stream()
                .map(MemberSettingRoomCategoryVO::getRoomCategoryId)
                .toList();
        for (MemberSettingMemberVO member : members) {
            member.setRoomCategoryIds(readMemberRoomCategories(campId, member.getUserId(), defaultIds));
        }
    }

    private List<String> readMemberRoomCategories(Long campId, String userId, List<String> defaultIds) {
        UserSystemConfig config = userSystemConfigMapper.selectOne(new LambdaQueryWrapper<UserSystemConfig>()
                .eq(UserSystemConfig::getCampId, campId)
                .eq(UserSystemConfig::getUserId, parseFlexibleUserId(userId))
                .eq(UserSystemConfig::getConfigKey, MEMBER_ROOM_CATEGORY_CONFIG_PREFIX + userId)
                .last("LIMIT 1"));
        if (config == null || !StringUtils.hasText(config.getConfigValue())) {
            return defaultIds;
        }
        try {
            List<?> parsed = objectMapper.readValue(config.getConfigValue(), List.class);
            return parsed.stream().map(String::valueOf).toList();
        } catch (JsonProcessingException ex) {
            throw new BusinessException(500, "成员房型配置解析失败");
        }
    }

    private void saveMemberRoomCategories(Long campId, Long memberUserId, List<String> roomCategoryIds) {
        String key = MEMBER_ROOM_CATEGORY_CONFIG_PREFIX + memberUserId;
        UserSystemConfig existing = userSystemConfigMapper.selectOne(new LambdaQueryWrapper<UserSystemConfig>()
                .eq(UserSystemConfig::getCampId, campId)
                .eq(UserSystemConfig::getUserId, memberUserId)
                .eq(UserSystemConfig::getConfigKey, key)
                .last("LIMIT 1"));
        String configValue;
        try {
            configValue = objectMapper.writeValueAsString(roomCategoryIds);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(500, "成员房型配置序列化失败");
        }

        if (existing == null) {
            UserSystemConfig created = new UserSystemConfig();
            created.setUserSystemConfigId(IdWorker.getId());
            created.setCampId(campId);
            created.setUserId(memberUserId);
            created.setConfigKey(key);
            created.setConfigValue(configValue);
            userSystemConfigMapper.insert(created);
            return;
        }

        UserSystemConfig update = new UserSystemConfig();
        update.setUserSystemConfigId(existing.getUserSystemConfigId());
        update.setConfigValue(configValue);
        userSystemConfigMapper.updateById(update);
    }

    private void validateDraft(Long campId, MemberSettingDraftRequest draft) {
        if (draft == null) {
            throw new BusinessException(40002, "draft is required");
        }
        normalizeRequiredText(draft.getName(), "name");
        normalizePhone(draft.getPhone());
        parseRequiredLong(draft.getRoleId(), "roleId");
        if (CollectionUtils.isEmpty(draft.getRoomCategoryIds())) {
            throw new BusinessException(40002, "roomCategoryIds is required");
        }
        List<String> availableRoomCategoryIds = loadRoomCategories(campId).stream()
                .map(MemberSettingRoomCategoryVO::getRoomCategoryId)
                .toList();
        if (!availableRoomCategoryIds.containsAll(draft.getRoomCategoryIds())) {
            throw new BusinessException(40002, "roomCategoryIds contains inaccessible room category");
        }
    }

    private PmsRole loadRole(Long campId, Long roleId) {
        PmsRole role = pmsRoleMapper.selectOne(new LambdaQueryWrapper<PmsRole>()
                .eq(PmsRole::getCampId, campId)
                .eq(PmsRole::getRoleId, roleId)
                .eq(PmsRole::getIsDeleted, NOT_DELETED)
                .eq(PmsRole::getStatus, ACTIVE_STATUS)
                .last("LIMIT 1"));
        if (role == null) {
            throw new BusinessException(40002, "roleId is invalid");
        }
        return role;
    }

    private PmsMember loadMemberByUserOrMobile(Long campId, String rawUserId, Long parsedUserId) {
        LambdaQueryWrapper<PmsMember> wrapper = new LambdaQueryWrapper<PmsMember>()
                .eq(PmsMember::getCampId, campId)
                .eq(PmsMember::getIsDeleted, NOT_DELETED)
                .eq(PmsMember::getStatus, ACTIVE_STATUS)
                .and(query -> {
                    if (parsedUserId != null) {
                        query.eq(PmsMember::getUserId, parsedUserId)
                                .or()
                                .eq(PmsMember::getMemberId, parsedUserId);
                        if (StringUtils.hasText(rawUserId)) {
                            query.or().eq(PmsMember::getMobile, rawUserId.trim());
                        }
                    } else {
                        query.eq(PmsMember::getMobile, rawUserId);
                    }
                })
                .last("LIMIT 1");
        PmsMember member = pmsMemberMapper.selectOne(wrapper);
        if (member == null) {
            throw new BusinessException(40404, "成员不存在");
        }
        return member;
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
            throw new BusinessException(40301, "无权访问当前门店成员设置");
        }
        return requestedCampId;
    }

    private String normalizeRouteMode(String routeMode) {
        return "edit".equals(routeMode) ? "edit" : "create".equals(routeMode) ? "create" : "list";
    }

    private String normalizeRoleNameFilter(String roleName) {
        if (!StringUtils.hasText(roleName) || ROLE_ALL_NAME.equals(roleName) || "all".equalsIgnoreCase(roleName)) {
            return null;
        }
        return roleName.trim();
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }

    private String normalizeRequiredText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(40002, fieldName + " is required");
        }
        String normalized = value.trim();
        if ("name".equals(fieldName) && !InputValidationUtils.isValidPersonName(normalized)) {
            throw new BusinessException(40002, "姓名格式不正确，请输入 2-30 个中文或英文字母");
        }
        return normalized;
    }

    private String normalizePhone(String phone) {
        String normalized = normalizeRequiredText(phone, "phone");
        if (!InputValidationUtils.isValidMainlandMobile(normalized)) {
            throw new BusinessException(40002, "手机号格式不正确");
        }
        return normalized;
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private Long parseRequiredLong(String value, String fieldName) {
        Long parsed = parseFlexibleUserId(value);
        if (parsed == null) {
            throw new BusinessException(40002, fieldName + " is required");
        }
        return parsed;
    }

    private Long parseLong(String value) {
        return StringUtils.hasText(value) ? Long.valueOf(value.trim()) : null;
    }

    private Long parseFlexibleUserId(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        if (!normalized.matches("^\\d+$")) {
            return null;
        }
        return Long.valueOf(normalized);
    }
}
