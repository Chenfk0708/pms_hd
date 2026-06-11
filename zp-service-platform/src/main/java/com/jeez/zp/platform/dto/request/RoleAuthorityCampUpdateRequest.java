package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoleAuthorityCampUpdateRequest {

    private String campId;
    private String roleId;
    private List<PermissionRow> permissionRows;

    @Data
    public static class PermissionRow {
        private String moduleId;
        private String moduleName;
        private List<String> permissions;
    }
}
