package com.jeez.zp.platform.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RolePermissionControllerIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Timeout(60)
    void roleCampGet_shouldReturnPagedRoleList() throws Exception {
        mockMvc.perform(post("/role/camp/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","keyword":"","pageNum":1,"pageSize":10}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.roles[0].roleId").value(13001))
                .andExpect(jsonPath("$.data.roles[0].roleName").value("系统管理员"))
                .andExpect(jsonPath("$.data.roles[0].description").value("拥有全部权限"))
                .andExpect(jsonPath("$.data.roles[0].memberCount").value(1))
                .andExpect(jsonPath("$.data.roles[0].canDelete").value(false))
                .andExpect(jsonPath("$.data.pagination.page").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(10))
                .andExpect(jsonPath("$.data.pagination.total").value(4));
    }

    @Test
    @Timeout(60)
    void roleAuthorityCampGet_shouldReturnPermissionRows() throws Exception {
        mockMvc.perform(post("/roleAuthority/camp/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","roleId":"13001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.roleId").value("13001"))
                .andExpect(jsonPath("$.data.roleName").value("系统管理员"))
                .andExpect(jsonPath("$.data.description").value("拥有全部权限"))
                .andExpect(jsonPath("$.data.permissionRows[0].moduleId").value("dashboard"))
                .andExpect(jsonPath("$.data.permissionRows[0].moduleName").value("工作台"))
                .andExpect(jsonPath("$.data.permissionRows[0].permissions[0]").value("查看"));
    }
}
