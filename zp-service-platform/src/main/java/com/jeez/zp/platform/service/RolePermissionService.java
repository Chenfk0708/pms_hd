package com.jeez.zp.platform.service;

import com.jeez.zp.platform.dto.request.RoleAuthorityCampUpdateRequest;
import com.jeez.zp.platform.vo.RoleAuthorityDetailVO;
import com.jeez.zp.platform.vo.RoleCampListVO;
import com.jeez.zp.platform.vo.CampRolesResponseVO;
import com.jeez.zp.platform.vo.RoleSummaryVO;

import java.util.List;

public interface RolePermissionService {

    RoleCampListVO getCampRoles(Long campId, Long userId, String keyword, Integer pageNum, Integer pageSize);

    CampRolesResponseVO getCampRoleOptions(Long campId, Long userId);

    RoleAuthorityDetailVO getRoleAuthorities(Long campId, Long userId, Long roleId);

    RoleAuthorityDetailVO updateRoleAuthorities(
            Long campId,
            Long userId,
            Long roleId,
            List<RoleAuthorityCampUpdateRequest.PermissionRow> permissionRows
    );

    RoleSummaryVO createRole(Long campId, Long userId, String roleName, String description);

    RoleSummaryVO updateRole(Long campId, Long userId, Long roleId, String roleName, String description);

    RoleSummaryVO deleteRole(Long campId, Long userId, Long roleId);
}
