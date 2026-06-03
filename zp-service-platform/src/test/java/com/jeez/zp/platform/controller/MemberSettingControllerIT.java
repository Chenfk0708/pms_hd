package com.jeez.zp.platform.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberSettingControllerIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Timeout(60)
    void bootstrap_shouldReturnMembersRolesAndRoomCategories() throws Exception {
        mockMvc.perform(post("/memberSettings/bootstrap")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "keyword":"",
                                  "roleName":"全部",
                                  "page":1,
                                  "pageSize":20,
                                  "routeMode":"list"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roles.length()", greaterThan(0)))
                .andExpect(jsonPath("$.data.members.length()", greaterThan(0)))
                .andExpect(jsonPath("$.data.roomCategories.length()", greaterThan(0)))
                .andExpect(jsonPath("$.data.editor.submitText").value("提交"));
    }

    @Test
    @Timeout(60)
    void saveAndBind_shouldPersistMemberChanges() throws Exception {
        String mobile = "13900009991";

        mockMvc.perform(post("/memberSettings/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "routeMode":"create",
                                  "draft":{
                                    "name":"联调成员",
                                    "phone":"%s",
                                    "roleId":"13001",
                                    "roleName":"管理员",
                                    "roomCategoryIds":["22001"]
                                  }
                                }
                                """.formatted(mobile)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.members[0].name").value("联调成员"))
                .andExpect(jsonPath("$.data.members[0].phone").value(mobile))
                .andExpect(jsonPath("$.data.members[0].wecomStatus").value("unbound"));

        mockMvc.perform(post("/memberSettings/wecom/bind")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","userId":"%s"}
                                """.formatted(mobile)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.members[0].phone").value(mobile))
                .andExpect(jsonPath("$.data.members[0].wecomStatus").value("bound"));
    }
}
