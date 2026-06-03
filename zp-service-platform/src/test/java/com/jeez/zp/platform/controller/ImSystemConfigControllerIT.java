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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ImSystemConfigControllerIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;
    private static final long USER_ID = 12001L;
    private static final String IM_CHANNELS_KEY = "hudson.im.picture.support.channels";
    private static final String IM_SHORTCUTS_KEY = "hudson.im.userShortcuts";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void commonsGet_shouldReturnConfiguredImSupportedChannels() throws Exception {
        resetImSystemConfig();
        jdbcTemplate.update("""
                        INSERT INTO system_config (
                            system_config_id,
                            camp_id,
                            config_key,
                            config_scope,
                            config_value,
                            value_type,
                            source,
                            updated_by,
                            updated_at
                        ) VALUES (?, ?, ?, 'camp', ?, 'json', 'platform', ?, NOW())
                        """,
                43201L,
                CAMP_ID,
                IM_CHANNELS_KEY,
                "[\"Tujia IM\",\"WeCom IM\"]",
                USER_ID
        );

        mockMvc.perform(post("/commons/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "code":"hudson.im.picture.support.channels"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.commons.length()").value(2))
                .andExpect(jsonPath("$.data.commons[0].codeName").value("Tujia IM"))
                .andExpect(jsonPath("$.data.commons[1].codeName").value("WeCom IM"));

        mockMvc.perform(post("/commons/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "code":"hudson.im.picture.support.channels"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Timeout(60)
    void systemConfigsUserShortcutGet_shouldReturnCurrentUserShortcutConfig() throws Exception {
        resetImSystemConfig();
        jdbcTemplate.update("""
                        INSERT INTO user_system_config (
                            user_system_config_id,
                            camp_id,
                            user_id,
                            config_key,
                            config_value,
                            updated_at
                        ) VALUES (?, ?, ?, ?, ?, NOW())
                        """,
                43211L,
                CAMP_ID,
                USER_ID,
                IM_SHORTCUTS_KEY,
                """
                        [
                          {
                            "code":0,
                            "name":"Activate",
                            "win":"Ctrl+Shift+1",
                            "mac":"Command+Shift+1",
                            "isOpen":1
                          }
                        ]
                        """
        );

        mockMvc.perform(post("/systemConfigs/user/shortcut/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":"12001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userShortcuts.length()").value(1))
                .andExpect(jsonPath("$.data.userShortcuts[0].code").value(0))
                .andExpect(jsonPath("$.data.userShortcuts[0].name").value("Activate"))
                .andExpect(jsonPath("$.data.userShortcuts[0].win").value("Ctrl+Shift+1"))
                .andExpect(jsonPath("$.data.userShortcuts[0].mac").value("Command+Shift+1"))
                .andExpect(jsonPath("$.data.userShortcuts[0].isOpen").value(1));

        mockMvc.perform(post("/systemConfigs/user/shortcut/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":"12002"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    private void resetImSystemConfig() {
        jdbcTemplate.update(
                "DELETE FROM system_config WHERE camp_id = ? AND config_key = ? AND config_scope = 'camp'",
                CAMP_ID,
                IM_CHANNELS_KEY
        );
        jdbcTemplate.update(
                "DELETE FROM user_system_config WHERE camp_id = ? AND user_id = ? AND config_key = ?",
                CAMP_ID,
                USER_ID,
                IM_SHORTCUTS_KEY
        );
    }
}
