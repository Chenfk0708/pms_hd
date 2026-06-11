package com.jeez.zp.platform.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void authMe_withoutGatewayHeaders_shouldReturn401() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @Timeout(60)
    void login_withSeedMobile_shouldReturnToken() throws Exception {
        Map<String, Object> original = snapshotSeedUser();
        try {
            resetSeedUserForAccountUpdate();

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"mobile":"13800000001","password":"demo-login"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.traceId").exists())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.data.loginMode").value("demo-direct"))
                    .andExpect(jsonPath("$.data.token").isNotEmpty())
                    .andExpect(jsonPath("$.data.tokenName").value("Authorization"))
                    .andExpect(jsonPath("$.data.userId").value(12001))
                    .andExpect(jsonPath("$.data.memberId").value(14001))
                    .andExpect(jsonPath("$.data.campId").value(10001))
                    .andExpect(jsonPath("$.data.roleCode").value("admin"))
                    .andExpect(jsonPath("$.data.roleName").value("系统管理员"));
        } finally {
            restoreSeedUser(original);
        }
    }

    @Test
    @Timeout(60)
    void login_withRootUsername_shouldReturnToken() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"root","password":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value("root"))
                .andExpect(jsonPath("$.data.userId").value(12001))
                .andExpect(jsonPath("$.data.roleCode").value("admin"))
                .andExpect(jsonPath("$.data.permissionCodes[0]").value("dashboard:view"));
    }

    @Test
    @Timeout(60)
    void register_withRole_shouldCreateUserMemberAndReturnRolePermissions() throws Exception {
        String username = "frontdesk_it";
        cleanupRegisteredUser(username, "13900000021");

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"frontdesk_it",
                                  "password":"123456",
                                  "nickName":"测试前台",
                                  "mobile":"13900000021",
                                  "email":"frontdesk-it@example.com",
                                  "campId":10001,
                                  "roleId":13003
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.roleCode").value("frontdesk"))
                .andExpect(jsonPath("$.data.roleName").value("前台"))
                .andExpect(jsonPath("$.data.permissionCodes[0]").value("dashboard:view"))
                .andExpect(jsonPath("$.data.permissionCodes").isArray())
                .andReturn();

        Long userId = objectMapper.readTree(result.getResponse().getContentAsString()).at("/data/userId").asLong();
        try {
            Integer userCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM pms_user WHERE user_id = ? AND username = ? AND mobile = ? AND is_deleted = 0",
                    Integer.class,
                    userId,
                    username,
                    "13900000021"
            );
            org.junit.jupiter.api.Assertions.assertEquals(1, userCount);

            Integer memberCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM pms_member WHERE user_id = ? AND camp_id = 10001 AND role_id = 13003 AND is_deleted = 0",
                    Integer.class,
                    userId
            );
            org.junit.jupiter.api.Assertions.assertEquals(1, memberCount);

            mockMvc.perform(get("/auth/me")
                            .header("X-Auth-Verified", "true")
                            .header("X-User-Id", String.valueOf(userId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.username").value(username))
                    .andExpect(jsonPath("$.data.roleCode").value("frontdesk"))
                    .andExpect(jsonPath("$.data.permissionCodes").isArray());
        } finally {
            cleanupRegisteredUser(username, "13900000021");
        }
    }

    @Test
    @Timeout(60)
    void register_withDuplicateUsername_shouldReject() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"root",
                                  "password":"123456",
                                  "nickName":"重复账号",
                                  "mobile":"13900000022",
                                  "email":"duplicate-root@example.com",
                                  "campId":10001,
                                  "roleId":13003
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40003))
                .andExpect(jsonPath("$.message").value("登录账号已存在"));
    }

    @Test
    @Timeout(60)
    void registerOptions_shouldReturnPublicRoleChoices() throws Exception {
        mockMvc.perform(get("/auth/register/options")
                        .param("campId", "10001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.campId").value(10001))
                .andExpect(jsonPath("$.data.roles[0].roleId").value(13001))
                .andExpect(jsonPath("$.data.roles[0].roleName").value("系统管理员"));
    }

    @Test
    @Timeout(60)
    void login_withUnknownMobile_shouldReturnBusinessError() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"mobile":"13999999999","password":"demo-login"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.message").value("账号不存在"));
    }

    @Test
    @Timeout(60)
    void authMe_response_shouldContainTraceIdAndCurrentUserContext() throws Exception {
        mockMvc.perform(get("/auth/me")
                        .header("X-Auth-Verified", "true")
                        .header("X-User-Id", "12001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.userId").value(12001))
                .andExpect(jsonPath("$.data.memberId").value(14001))
                .andExpect(jsonPath("$.data.campId").value(10001))
                .andExpect(jsonPath("$.data.campName").isNotEmpty())
                .andExpect(jsonPath("$.data.poiId").value(11001))
                .andExpect(jsonPath("$.data.poiName").isNotEmpty())
                .andExpect(jsonPath("$.data.roleCode").value("admin"))
                .andExpect(jsonPath("$.data.roleName").value("系统管理员"))
                .andExpect(jsonPath("$.data.permissionCodes[0]").value("dashboard:view"));
    }

    @Test
    @Timeout(60)
    void updateAccount_withWrongOldPassword_shouldRejectPasswordChange() throws Exception {
        Map<String, Object> original = snapshotSeedUser();
        Map<String, Object> originalMember = snapshotSeedMember();
        try {
            resetSeedUserForAccountUpdate();

            mockMvc.perform(post("/auth/account")
                            .header("X-Auth-Verified", "true")
                            .header("X-User-Id", "12001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nickName":"Account Settings User",
                                      "email":"account-settings@example.com",
                                      "wechat":"sy-wechat",
                                      "oldPassword":"wrong-password",
                                      "newPassword":"new-login-123"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(40004));

            String passwordHash = jdbcTemplate.queryForObject(
                    "SELECT password_hash FROM pms_user WHERE user_id = 12001",
                    String.class
            );
            org.junit.jupiter.api.Assertions.assertNull(passwordHash);
        } finally {
            restoreSeedUser(original);
            restoreSeedMember(originalMember);
        }
    }

    @Test
    @Timeout(60)
    void updateAccount_shouldRejectInvalidNameAndEmailBeforePersisting() throws Exception {
        Map<String, Object> original = snapshotSeedUser();
        Map<String, Object> originalMember = snapshotSeedMember();
        try {
            resetSeedUserForAccountUpdate();

            mockMvc.perform(post("/auth/account")
                            .header("X-Auth-Verified", "true")
                            .header("X-User-Id", "12001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nickName":"A1",
                                      "email":"valid@example.com"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("姓名格式不正确，请输入 2-30 个中文或英文字母"));

            mockMvc.perform(post("/auth/account")
                            .header("X-Auth-Verified", "true")
                            .header("X-User-Id", "12001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nickName":"账号设置人",
                                      "email":"bad-email"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("邮箱格式不正确"));

            Map<String, Object> user = jdbcTemplate.queryForMap("""
                    SELECT nick_name, email
                    FROM pms_user
                    WHERE user_id = 12001
                    """);
            org.junit.jupiter.api.Assertions.assertEquals("Demo Admin", user.get("nick_name"));
            org.junit.jupiter.api.Assertions.assertEquals("demo-admin@example.com", user.get("email"));
        } finally {
            restoreSeedUser(original);
            restoreSeedMember(originalMember);
        }
    }

    @Test
    @Timeout(60)
    void updateAccount_withNewPassword_shouldPersistProfileAndAllowNewPasswordLogin() throws Exception {
        Map<String, Object> original = snapshotSeedUser();
        Map<String, Object> originalMember = snapshotSeedMember();
        try {
            resetSeedUserForAccountUpdate();

            mockMvc.perform(post("/auth/account")
                            .header("X-Auth-Verified", "true")
                            .header("X-User-Id", "12001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nickName":"Account Settings User",
                                      "email":"account-settings@example.com",
                                      "wechat":"sy-wechat",
                                      "avatarUrl":"data:image/png;base64,AA==",
                                      "oldPassword":"demo-login",
                                      "newPassword":"new-login-123"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.userId").value(12001))
                    .andExpect(jsonPath("$.data.nickName").value("Account Settings User"))
                    .andExpect(jsonPath("$.data.email").value("account-settings@example.com"))
                    .andExpect(jsonPath("$.data.wechat").value("sy-wechat"))
                    .andExpect(jsonPath("$.data.avatarUrl").value("data:image/png;base64,AA=="))
                    .andExpect(jsonPath("$.data.passwordSet").value(true));

            Map<String, Object> linkedMember = jdbcTemplate.queryForMap("""
                    SELECT name, email
                    FROM pms_member
                    WHERE user_id = 12001
                    """);
            org.junit.jupiter.api.Assertions.assertEquals("Account Settings User", linkedMember.get("name"));
            org.junit.jupiter.api.Assertions.assertEquals("account-settings@example.com", linkedMember.get("email"));

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"mobile":"13800000001","password":"demo-login"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(40002));

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"mobile":"13800000001","password":"new-login-123"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.nickName").value("Account Settings User"))
                    .andExpect(jsonPath("$.data.email").value("account-settings@example.com"))
                    .andExpect(jsonPath("$.data.wechat").value("sy-wechat"))
                    .andExpect(jsonPath("$.data.passwordSet").value(true));
        } finally {
            restoreSeedUser(original);
            restoreSeedMember(originalMember);
        }
    }

    @Test
    @Timeout(60)
    void updateAccount_withLongAvatarDataUrl_shouldPersistProfileWithoutPasswordChange() throws Exception {
        Map<String, Object> original = snapshotSeedUser();
        Map<String, Object> originalMember = snapshotSeedMember();
        try {
            resetSeedUserForAccountUpdate();

            String avatarUrl = "data:image/png;base64," + "A".repeat(600);
            mockMvc.perform(post("/auth/account")
                            .header("X-Auth-Verified", "true")
                            .header("X-User-Id", "12001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "nickName", "Avatar Upload User",
                                    "email", "avatar-upload@example.com",
                                    "wechat", "avatar-wechat",
                                    "avatarUrl", avatarUrl
                            ))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.avatarUrl").value(avatarUrl))
                    .andExpect(jsonPath("$.data.passwordSet").value(false));

            String storedAvatarUrl = jdbcTemplate.queryForObject(
                    "SELECT avatar_url FROM pms_user WHERE user_id = 12001",
                    String.class
            );
            org.junit.jupiter.api.Assertions.assertEquals(avatarUrl, storedAvatarUrl);
        } finally {
            restoreSeedUser(original);
            restoreSeedMember(originalMember);
        }
    }

    @Test
    @Timeout(60)
    void logout_withValidToken_shouldClearTokenSession() throws Exception {
        Map<String, Object> original = snapshotSeedUser();
        try {
            resetSeedUserForAccountUpdate();

            MvcResult loginResult = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"mobile":"13800000001","password":"demo-login"}
                                    """))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
            String token = loginJson.at("/data/token").asText();

            mockMvc.perform(post("/auth/logout")
                            .header("Authorization", "Bearer " + token)
                            .header("X-Auth-Verified", "true")
                            .header("X-User-Id", "12001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.traceId").exists())
                    .andExpect(jsonPath("$.timestamp").exists());

            org.junit.jupiter.api.Assertions.assertNull(StpUtil.getLoginIdByToken(token));
        } finally {
            restoreSeedUser(original);
        }
    }

    private Map<String, Object> snapshotSeedUser() {
        return jdbcTemplate.queryForMap("""
                SELECT nick_name, email, avatar_url, wechat, password_hash
                FROM pms_user
                WHERE user_id = 12001
                """);
    }

    private Map<String, Object> snapshotSeedMember() {
        return jdbcTemplate.queryForMap("""
                SELECT name, email
                FROM pms_member
                WHERE user_id = 12001
                """);
    }

    private void resetSeedUserForAccountUpdate() {
        jdbcTemplate.update("""
                UPDATE pms_user
                SET nick_name = ?, email = ?, avatar_url = NULL, wechat = NULL, password_hash = NULL
                WHERE user_id = 12001
                """, "Demo Admin", "demo-admin@example.com");
    }

    private void restoreSeedUser(Map<String, Object> original) {
        jdbcTemplate.update("""
                UPDATE pms_user
                SET nick_name = ?, email = ?, avatar_url = ?, wechat = ?, password_hash = ?
                WHERE user_id = 12001
                """,
                original.get("nick_name"),
                original.get("email"),
                original.get("avatar_url"),
                original.get("wechat"),
                original.get("password_hash")
        );
    }

    private void restoreSeedMember(Map<String, Object> original) {
        jdbcTemplate.update("""
                UPDATE pms_member
                SET name = ?, email = ?
                WHERE user_id = 12001
                """,
                original.get("name"),
                original.get("email")
        );
    }

    private void cleanupRegisteredUser(String username, String mobile) {
        jdbcTemplate.update("""
                DELETE m
                FROM pms_member m
                JOIN pms_user u ON u.user_id = m.user_id
                WHERE u.username = ? OR u.mobile = ?
                """, username, mobile);
        jdbcTemplate.update("""
                DELETE FROM pms_user
                WHERE username = ? OR mobile = ?
                """, username, mobile);
    }
}
