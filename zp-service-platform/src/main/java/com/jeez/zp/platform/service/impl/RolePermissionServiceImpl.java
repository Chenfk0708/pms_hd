package com.jeez.zp.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.jeez.zp.platform.dto.request.RoleAuthorityCampUpdateRequest;
import com.jeez.zp.platform.entity.PmsMember;
import com.jeez.zp.platform.entity.PmsRole;
import com.jeez.zp.platform.entity.PmsRoleAuthority;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.PmsMemberMapper;
import com.jeez.zp.platform.mapper.PmsRoleAuthorityMapper;
import com.jeez.zp.platform.mapper.PmsRoleMapper;
import com.jeez.zp.platform.mapper.RolePermissionMapper;
import com.jeez.zp.platform.service.RolePermissionService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.PaginationVO;
import com.jeez.zp.platform.vo.PermissionRowVO;
import com.jeez.zp.platform.vo.CampRolesResponseVO;
import com.jeez.zp.platform.vo.RoleAuthorityDetailVO;
import com.jeez.zp.platform.vo.RoleCampListVO;
import com.jeez.zp.platform.vo.RolePermissionGrantVO;
import com.jeez.zp.platform.vo.RolePermissionOptionVO;
import com.jeez.zp.platform.vo.RoleSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int ACTIVE_STATUS = 1;
    private static final int INACTIVE_STATUS = 0;
    private static final int NOT_DELETED = 0;
    private static final int DELETED = 1;
    private static final int SYSTEM_ROLE = 1;
    private static final String DEFAULT_ROLE_DESCRIPTION = "自定义角色";
    private static final String PERMISSION_KEY_SEPARATOR = "\u001F";

    private final RolePermissionMapper rolePermissionMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;
    private final PmsRoleMapper pmsRoleMapper;
    private final PmsMemberMapper pmsMemberMapper;
    private final PmsRoleAuthorityMapper pmsRoleAuthorityMapper;

    @Override
    public RoleCampListVO getCampRoles(Long campId, Long userId, String keyword, Integer pageNum, Integer pageSize) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<RoleSummaryVO> roles = rolePermissionMapper.selectRoleSummaries(resolvedCampId, normalizeKeyword(keyword));

        int resolvedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int resolvedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        int total = roles.size();
        int fromIndex = Math.min((resolvedPageNum - 1) * resolvedPageSize, total);
        int toIndex = Math.min(fromIndex + resolvedPageSize, total);

        RoleCampListVO response = new RoleCampListVO();
        response.setRoles(new ArrayList<>(roles.subList(fromIndex, toIndex)));
        response.setPagination(new PaginationVO(resolvedPageNum, resolvedPageSize, total));
        return response;
    }

    @Override
    public CampRolesResponseVO getCampRoleOptions(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        CampRolesResponseVO response = new CampRolesResponseVO();
        response.setRoles(rolePermissionMapper.selectRoleSummaries(resolvedCampId, null));
        response.setEmployees(rolePermissionMapper.selectCampEmployees(resolvedCampId));
        return response;
    }

    @Override
    public RoleAuthorityDetailVO getRoleAuthorities(Long campId, Long userId, Long roleId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        RoleSummaryVO roleSummary = rolePermissionMapper.selectRoleSummary(resolvedCampId, roleId);
        if (roleSummary == null) {
            throw new BusinessException(40006, "当前角色不存在，请刷新后重试");
        }

        List<RolePermissionGrantVO> grants = rolePermissionMapper.selectRolePermissionGrants(resolvedCampId, roleId);
        List<RolePermissionOptionVO> options = rolePermissionMapper.selectPermissionOptions();
        Map<String, PermissionRowVO> rowMap = buildAvailablePermissionRows(options);
        for (RolePermissionGrantVO grant : grants) {
            PermissionRowVO row = rowMap.computeIfAbsent(
                    grant.getModuleId(),
                    key -> new PermissionRowVO(grant.getModuleId(), grant.getModuleName(), new ArrayList<>(), new ArrayList<>())
            );
            if (!row.getPermissions().contains(grant.getPermissionLabel())) {
                row.getPermissions().add(grant.getPermissionLabel());
            }
            if (!row.getAvailablePermissions().contains(grant.getPermissionLabel())) {
                row.getAvailablePermissions().add(grant.getPermissionLabel());
            }
        }

        RoleAuthorityDetailVO response = new RoleAuthorityDetailVO();
        response.setRoleId(String.valueOf(roleSummary.getRoleId()));
        response.setRoleName(roleSummary.getRoleName());
        response.setDescription(roleSummary.getDescription());
        response.setPermissionRows(new ArrayList<>(rowMap.values()));
        return response;
    }

    @Override
    @Transactional
    public RoleAuthorityDetailVO updateRoleAuthorities(
            Long campId,
            Long userId,
            Long roleId,
            List<RoleAuthorityCampUpdateRequest.PermissionRow> permissionRows
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        loadRoleOrThrow(resolvedCampId, roleId);

        List<RolePermissionOptionVO> options = rolePermissionMapper.selectPermissionOptions();
        Map<String, RolePermissionOptionVO> optionMap = buildPermissionOptionMap(options);
        Set<Long> selectedAuthorityIds = resolveSelectedAuthorityIds(permissionRows, optionMap);

        pmsRoleAuthorityMapper.delete(new LambdaQueryWrapper<PmsRoleAuthority>()
                .eq(PmsRoleAuthority::getCampId, resolvedCampId)
                .eq(PmsRoleAuthority::getRoleId, roleId));

        LocalDateTime now = LocalDateTime.now();
        for (RolePermissionOptionVO option : options) {
            if (!selectedAuthorityIds.contains(option.getAuthorityId())) {
                continue;
            }
            PmsRoleAuthority roleAuthority = new PmsRoleAuthority();
            roleAuthority.setId(IdWorker.getId());
            roleAuthority.setCampId(resolvedCampId);
            roleAuthority.setRoleId(roleId);
            roleAuthority.setAuthorityId(option.getAuthorityId());
            roleAuthority.setAuthorityType(option.getAuthorityType());
            roleAuthority.setCreatedAt(now);
            roleAuthority.setCreatedBy(userId);
            pmsRoleAuthorityMapper.insert(roleAuthority);
        }

        PmsRole update = new PmsRole();
        update.setRoleId(roleId);
        update.setUpdatedBy(userId);
        pmsRoleMapper.updateById(update);

        return getRoleAuthorities(resolvedCampId, userId, roleId);
    }

    @Override
    @Transactional
    public RoleSummaryVO createRole(Long campId, Long userId, String roleName, String description) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        String normalizedRoleName = normalizeRoleName(roleName);
        ensureRoleNamePresent(normalizedRoleName);
        ensureRoleNameUnique(resolvedCampId, normalizedRoleName, null);

        Long roleId = IdWorker.getId();
        PmsRole role = new PmsRole();
        role.setRoleId(roleId);
        role.setCampId(resolvedCampId);
        role.setRoleName(normalizedRoleName);
        role.setRoleCode("custom-" + roleId);
        role.setIsSystem(0);
        role.setStatus(ACTIVE_STATUS);
        role.setRemark(normalizeDescription(description));
        role.setCreatedBy(userId);
        role.setUpdatedBy(userId);
        role.setIsDeleted(NOT_DELETED);
        role.setVersionNo(0);
        pmsRoleMapper.insert(role);

        return loadRoleSummaryOrThrow(resolvedCampId, roleId);
    }

    @Override
    @Transactional
    public RoleSummaryVO updateRole(Long campId, Long userId, Long roleId, String roleName, String description) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        String normalizedRoleName = normalizeRoleName(roleName);
        ensureRoleNamePresent(normalizedRoleName);

        PmsRole existingRole = loadRoleOrThrow(resolvedCampId, roleId);
        ensureRoleNameUnique(resolvedCampId, normalizedRoleName, roleId);

        PmsRole update = new PmsRole();
        update.setRoleId(existingRole.getRoleId());
        update.setRoleName(normalizedRoleName);
        update.setRemark(normalizeDescription(description));
        update.setUpdatedBy(userId);
        pmsRoleMapper.updateById(update);

        return loadRoleSummaryOrThrow(resolvedCampId, roleId);
    }

    @Override
    @Transactional
    public RoleSummaryVO deleteRole(Long campId, Long userId, Long roleId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        PmsRole role = loadRoleOrThrow(resolvedCampId, roleId);
        RoleSummaryVO summary = loadRoleSummaryOrThrow(resolvedCampId, roleId);

        if (SYSTEM_ROLE == role.getIsSystem()) {
            throw new BusinessException(40004, "系统内置角色不允许删除");
        }
        if (countActiveMembers(resolvedCampId, roleId) > 0) {
            throw new BusinessException(40005, "当前角色已关联成员，无法删除");
        }

        pmsRoleAuthorityMapper.delete(new LambdaQueryWrapper<PmsRoleAuthority>()
                .eq(PmsRoleAuthority::getCampId, resolvedCampId)
                .eq(PmsRoleAuthority::getRoleId, roleId));

        PmsRole update = new PmsRole();
        update.setRoleId(roleId);
        update.setStatus(INACTIVE_STATUS);
        update.setIsDeleted(DELETED);
        update.setUpdatedBy(userId);
        pmsRoleMapper.updateById(update);

        return summary;
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
            throw new BusinessException(40301, "无权访问当前租户角色数据");
        }
        return requestedCampId;
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }

    private String normalizeRoleName(String roleName) {
        return StringUtils.hasText(roleName) ? roleName.trim() : null;
    }

    private String normalizeDescription(String description) {
        return StringUtils.hasText(description) ? description.trim() : DEFAULT_ROLE_DESCRIPTION;
    }

    private void ensureRoleNamePresent(String roleName) {
        if (!StringUtils.hasText(roleName)) {
            throw new BusinessException(40002, "请输入角色名称");
        }
    }

    private void ensureRoleNameUnique(Long campId, String roleName, Long excludedRoleId) {
        Long count = pmsRoleMapper.selectCount(new LambdaQueryWrapper<PmsRole>()
                .eq(PmsRole::getCampId, campId)
                .eq(PmsRole::getRoleName, roleName)
                .eq(PmsRole::getIsDeleted, NOT_DELETED)
                .ne(excludedRoleId != null, PmsRole::getRoleId, excludedRoleId));
        if (count != null && count > 0) {
            throw new BusinessException(40003, "角色“" + roleName + "”已存在，请更换名称");
        }
    }

    private PmsRole loadRoleOrThrow(Long campId, Long roleId) {
        PmsRole role = pmsRoleMapper.selectOne(new LambdaQueryWrapper<PmsRole>()
                .eq(PmsRole::getCampId, campId)
                .eq(PmsRole::getRoleId, roleId)
                .eq(PmsRole::getIsDeleted, NOT_DELETED)
                .last("LIMIT 1"));
        if (role == null) {
            throw new BusinessException(40006, "当前角色不存在，请刷新后重试");
        }
        return role;
    }

    private RoleSummaryVO loadRoleSummaryOrThrow(Long campId, Long roleId) {
        RoleSummaryVO roleSummary = rolePermissionMapper.selectRoleSummary(campId, roleId);
        if (roleSummary == null) {
            throw new BusinessException(40006, "当前角色不存在，请刷新后重试");
        }
        return roleSummary;
    }

    private long countActiveMembers(Long campId, Long roleId) {
        Long count = pmsMemberMapper.selectCount(new LambdaQueryWrapper<PmsMember>()
                .eq(PmsMember::getCampId, campId)
                .eq(PmsMember::getRoleId, roleId)
                .eq(PmsMember::getIsDeleted, NOT_DELETED)
                .eq(PmsMember::getStatus, ACTIVE_STATUS));
        return count == null ? 0 : count;
    }

    private Map<String, PermissionRowVO> buildAvailablePermissionRows(List<RolePermissionOptionVO> options) {
        Map<String, PermissionRowVO> rowMap = new LinkedHashMap<>();
        for (RolePermissionOptionVO option : options) {
            PermissionRowVO row = rowMap.computeIfAbsent(
                    option.getModuleId(),
                    key -> new PermissionRowVO(option.getModuleId(), option.getModuleName(), new ArrayList<>(), new ArrayList<>())
            );
            if (!row.getAvailablePermissions().contains(option.getPermissionLabel())) {
                row.getAvailablePermissions().add(option.getPermissionLabel());
            }
        }
        return rowMap;
    }

    private Map<String, RolePermissionOptionVO> buildPermissionOptionMap(List<RolePermissionOptionVO> options) {
        return options.stream()
                .collect(Collectors.toMap(
                        option -> permissionKey(option.getModuleId(), option.getPermissionLabel()),
                        option -> option,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
    }

    private Set<Long> resolveSelectedAuthorityIds(
            List<RoleAuthorityCampUpdateRequest.PermissionRow> rows,
            Map<String, RolePermissionOptionVO> optionMap
    ) {
        Set<Long> selectedAuthorityIds = new HashSet<>();
        if (rows == null) {
            return selectedAuthorityIds;
        }

        for (RoleAuthorityCampUpdateRequest.PermissionRow row : rows) {
            String moduleId = normalizePermissionPart(row.getModuleId());
            List<String> permissions = row.getPermissions();
            if (!StringUtils.hasText(moduleId) || permissions == null) {
                continue;
            }
            for (String permission : permissions) {
                String permissionLabel = normalizePermissionPart(permission);
                if (!StringUtils.hasText(permissionLabel)) {
                    continue;
                }
                RolePermissionOptionVO option = optionMap.get(permissionKey(moduleId, permissionLabel));
                if (option == null) {
                    throw new BusinessException(40007, "权限项不存在，请刷新后重试");
                }
                selectedAuthorityIds.add(option.getAuthorityId());
            }
        }
        return selectedAuthorityIds;
    }

    private String normalizePermissionPart(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String permissionKey(String moduleId, String permissionLabel) {
        return moduleId + PERMISSION_KEY_SEPARATOR + permissionLabel;
    }
}
