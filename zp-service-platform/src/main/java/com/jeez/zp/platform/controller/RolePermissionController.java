package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.RoleAuthorityCampRequest;
import com.jeez.zp.platform.dto.request.RoleCampCreateRequest;
import com.jeez.zp.platform.dto.request.RoleCampDeleteRequest;
import com.jeez.zp.platform.dto.request.RoleCampPageRequest;
import com.jeez.zp.platform.dto.request.RoleCampUpdateRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.RolePermissionService;
import com.jeez.zp.platform.vo.RoleAuthorityDetailVO;
import com.jeez.zp.platform.vo.CampRolesResponseVO;
import com.jeez.zp.platform.vo.RoleCampListVO;
import com.jeez.zp.platform.vo.RoleSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @PostMapping("/role/camp/get")
    public HudsonResponse<RoleCampListVO> getCampRoles(@RequestBody RoleCampPageRequest request) {
        return HudsonResponse.success(
                rolePermissionService.getCampRoles(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getKeyword(),
                        request.getPageNum(),
                        request.getPageSize()
                ),
                TraceIdFactory.next("role-camp-get")
        );
    }

    @PostMapping("/campRoles/get")
    public HudsonResponse<CampRolesResponseVO> getCampRoleOptions(@RequestBody RoleCampPageRequest request) {
        return HudsonResponse.success(
                rolePermissionService.getCampRoleOptions(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId()
                ),
                TraceIdFactory.next("camp-roles-get")
        );
    }

    @PostMapping("/roleAuthority/camp/get")
    public HudsonResponse<RoleAuthorityDetailVO> getRoleAuthorities(@RequestBody RoleAuthorityCampRequest request) {
        return HudsonResponse.success(
                rolePermissionService.getRoleAuthorities(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseRequiredLong(request.getRoleId())
                ),
                TraceIdFactory.next("role-authority-camp-get")
        );
    }

    @PostMapping("/role/camp/create")
    public HudsonResponse<RoleSummaryVO> createRole(@RequestBody RoleCampCreateRequest request) {
        return HudsonResponse.success(
                rolePermissionService.createRole(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        request.getRoleName(),
                        request.getDescription()
                ),
                TraceIdFactory.next("role-camp-create")
        );
    }

    @PostMapping("/role/camp/update")
    public HudsonResponse<RoleSummaryVO> updateRole(@RequestBody RoleCampUpdateRequest request) {
        return HudsonResponse.success(
                rolePermissionService.updateRole(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseRequiredLong(request.getRoleId()),
                        request.getRoleName(),
                        request.getDescription()
                ),
                TraceIdFactory.next("role-camp-update")
        );
    }

    @PostMapping("/role/camp/delete")
    public HudsonResponse<RoleSummaryVO> deleteRole(@RequestBody RoleCampDeleteRequest request) {
        return HudsonResponse.success(
                rolePermissionService.deleteRole(
                        parseLong(request.getCampId()),
                        LoginUserContext.requiredUserId(),
                        parseRequiredLong(request.getRoleId())
                ),
                TraceIdFactory.next("role-camp-delete")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }

    private Long parseRequiredLong(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("roleId 不能为空");
        }
        return Long.valueOf(value);
    }
}
