package com.jeez.zp.platform.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    void bootstrap_shouldPreferCurrentAccountProfileForLinkedMember() throws Exception {
        jdbcTemplate.update("""
                UPDATE pms_user
                SET nick_name = ?, email = ?
                WHERE user_id = 12001
                """, "账号设置新姓名", "account-updated@example.com");
        jdbcTemplate.update("""
                UPDATE pms_member
                SET name = ?, email = ?
                WHERE user_id = 12001
                """, "成员旧姓名", "member-old@example.com");

        mockMvc.perform(post("/memberSettings/bootstrap")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "keyword":"账号设置新姓名",
                                  "roleName":"全部",
                                  "page":1,
                                  "pageSize":20,
                                  "routeMode":"list"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.members.length()").value(1))
                .andExpect(jsonPath("$.data.members[0].userId").value("12001"))
                .andExpect(jsonPath("$.data.members[0].name").value("账号设置新姓名"))
                .andExpect(jsonPath("$.data.members[0].email").value("account-updated@example.com"));
    }

    @Test
    @Timeout(60)
    void saveAndBind_shouldPersistMemberChanges() throws Exception {
        String mobile = "139" + String.valueOf(System.currentTimeMillis()).substring(5, 13);
        String roomCategoryId = jdbcTemplate.queryForObject("""
                SELECT CAST(room_category_id AS CHAR)
                FROM room_category
                WHERE camp_id = 10001
                  AND is_deleted = 0
                  AND status = 1
                ORDER BY sort_no ASC, room_category_id ASC
                LIMIT 1
                """, String.class);

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
                                    "roomCategoryIds":["%s"]
                                  }
                                }
                                """.formatted(mobile, roomCategoryId)))
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

    @Test
    @Timeout(60)
    void save_shouldRejectInvalidNameAndPhoneBeforeInsert() throws Exception {
        String roomCategoryId = jdbcTemplate.queryForObject("""
                SELECT CAST(room_category_id AS CHAR)
                FROM room_category
                WHERE camp_id = 10001
                  AND is_deleted = 0
                  AND status = 1
                ORDER BY sort_no ASC, room_category_id ASC
                LIMIT 1
                """, String.class);

        mockMvc.perform(post("/memberSettings/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "routeMode":"create",
                                  "draft":{
                                    "name":"成员1",
                                    "phone":"13900009991",
                                    "roleId":"13001",
                                    "roleName":"管理员",
                                    "roomCategoryIds":["%s"]
                                  }
                                }
                                """.formatted(roomCategoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("姓名格式不正确，请输入 2-30 个中文或英文字母"));

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
                                    "phone":"12000000000",
                                    "roleId":"13001",
                                    "roleName":"管理员",
                                    "roomCategoryIds":["%s"]
                                  }
                                }
                                """.formatted(roomCategoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("手机号格式不正确"));

        org.junit.jupiter.api.Assertions.assertEquals(0, jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM pms_member
                WHERE camp_id = 10001
                  AND mobile IN ('13900009991', '12000000000')
                """, Integer.class));
    }
}
