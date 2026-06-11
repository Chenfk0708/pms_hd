package com.jeez.zp.platform.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRowVO {

    private String moduleId;
    private String moduleName;
    private List<String> permissions;
    private List<String> availablePermissions;

    public PermissionRowVO(String moduleId, String moduleName, List<String> permissions) {
        this(moduleId, moduleName, permissions, permissions);
    }
}
