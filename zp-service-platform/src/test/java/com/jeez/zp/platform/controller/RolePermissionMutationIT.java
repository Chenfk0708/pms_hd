package com.jeez.zp.platform.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RolePermissionMutationIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String CAMP_ID = "10001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Timeout(60)
    void roleCampCreate_shouldPersistRoleSummary() throws Exception {
        String roleName = "夜班管家-" + System.nanoTime();

        MvcResult createResult = mockMvc.perform(post("/role/camp/create")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","roleName":"%s","description":"负责夜班运营值守"}
                                """.formatted(roleName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roleName").value(roleName))
                .andExpect(jsonPath("$.data.description").value("负责夜班运营值守"))
                .andExpect(jsonPath("$.data.memberCount").value(0))
                .andExpect(jsonPath("$.data.canDelete").value(true))
                .andReturn();

        String roleId = objectMapper.readTree(createResult.getResponse().getContentAsByteArray())
                .at("/data/roleId")
                .asText();

        mockMvc.perform(post("/role/camp/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","keyword":"%s","pageNum":1,"pageSize":10}
                                """.formatted(roleName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roles[0].roleId").value(Long.parseLong(roleId)))
                .andExpect(jsonPath("$.data.roles[0].roleName").value(roleName))
                .andExpect(jsonPath("$.data.pagination.total").value(1));
    }

    @Test
    @Timeout(60)
    void roleCampUpdate_shouldRenameCustomRole() throws Exception {
        JsonNode created = createRole("保洁主管-" + System.nanoTime(), "初始描述");
        String roleId = created.path("roleId").asText();
        String renamed = "保洁主管-改名-" + System.nanoTime();

        mockMvc.perform(post("/role/camp/update")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","roleId":"%s","roleName":"%s","description":"更新后的描述"}
                                """.formatted(roleId, renamed)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roleId").value(Long.parseLong(roleId)))
                .andExpect(jsonPath("$.data.roleName").value(renamed))
                .andExpect(jsonPath("$.data.description").value("更新后的描述"));

        mockMvc.perform(post("/roleAuthority/camp/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","roleId":"%s"}
                                """.formatted(roleId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roleName").value(renamed))
                .andExpect(jsonPath("$.data.description").value("更新后的描述"));
    }

    @Test
    @Timeout(60)
    void roleAuthorityCampUpdate_shouldPersistSelectedPermissions() throws Exception {
        JsonNode created = createRole("权限测试-" + System.nanoTime(), "用于权限勾选测试");
        String roleId = created.path("roleId").asText();

        mockMvc.perform(post("/roleAuthority/camp/update")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "roleId":"%s",
                                  "permissionRows":[
                                    {"moduleId":"dashboard","moduleName":"工作台","permissions":["查看"]},
                                    {"moduleId":"room","moduleName":"房态管理","permissions":["查看"]}
                                  ]
                                }
                                """.formatted(roleId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roleId").value(roleId))
                .andExpect(jsonPath("$.data.permissionRows[0].moduleId").value("dashboard"))
                .andExpect(jsonPath("$.data.permissionRows[0].permissions[0]").value("查看"))
                .andExpect(jsonPath("$.data.permissionRows[1].moduleId").value("room"))
                .andExpect(jsonPath("$.data.permissionRows[1].permissions[0]").value("查看"))
                .andExpect(jsonPath("$.data.permissionRows[1].availablePermissions[0]").value("查看"))
                .andExpect(jsonPath("$.data.permissionRows[1].availablePermissions[1]").value("操作"));

        mockMvc.perform(post("/roleAuthority/camp/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","roleId":"%s"}
                                """.formatted(roleId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.permissionRows[0].permissions[0]").value("查看"))
                .andExpect(jsonPath("$.data.permissionRows[1].moduleId").value("room"))
                .andExpect(jsonPath("$.data.permissionRows[1].permissions[0]").value("查看"))
                .andExpect(jsonPath("$.data.permissionRows[1].permissions.length()").value(1));
    }

    @Test
    @Timeout(60)
    void roleCampDelete_shouldRemoveCustomRole() throws Exception {
        JsonNode created = createRole("值班前台-" + System.nanoTime(), "待删除角色");
        String roleId = created.path("roleId").asText();
        String roleName = created.path("roleName").asText();

        mockMvc.perform(post("/role/camp/delete")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","roleId":"%s"}
                                """.formatted(roleId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roleId").value(Long.parseLong(roleId)))
                .andExpect(jsonPath("$.data.roleName").value(roleName));

        mockMvc.perform(post("/role/camp/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","keyword":"%s","pageNum":1,"pageSize":10}
                                """.formatted(roleName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.pagination.total").value(0));
    }

    @Test
    @Timeout(60)
    void roleCampDelete_shouldRejectSystemRole() throws Exception {
        mockMvc.perform(post("/role/camp/delete")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","roleId":"13001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40004))
                .andExpect(jsonPath("$.message").value("系统内置角色不允许删除"));
    }

    private JsonNode createRole(String roleName, String description) throws Exception {
        MvcResult result = mockMvc.perform(post("/role/camp/create")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"%s","roleName":"%s","description":"%s"}
                                """.formatted(CAMP_ID, roleName, description)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data");
    }
}
