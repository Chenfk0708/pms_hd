package com.jeez.zp.crm.controller;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserAuthorityNotificationIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;
    private static final long USER_ID = 12001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void userAuthorityNotification_shouldListExcludeAndRestoreNotificationAuthorities() throws Exception {
        seedAuthorities();

        mockMvc.perform(post("/userAuthority/notification/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.modules.length()").value(1))
                .andExpect(jsonPath("$.data.modules[0].moduleName").value("CRM通知"))
                .andExpect(jsonPath("$.data.modules[0].items[0].authorityId").value("43001"))
                .andExpect(jsonPath("$.data.modules[0].items[0].excluded").value(false));

        mockMvc.perform(post("/userAuthority/exclude")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "authorityIds":["43001"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        assertThat(excludeCount()).isEqualTo(1);

        mockMvc.perform(delete("/userAuthority/exclude")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "authorityIds":["43001"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        assertThat(excludeCount()).isZero();
    }

    private void seedAuthorities() {
        resetAuthorities();
        jdbcTemplate.update("""
                        INSERT INTO authority_dict (
                            authority_id,
                            authority_name,
                            authority_code,
                            authority_type,
                            module_name,
                            remark,
                            seq_no,
                            status,
                            created_at
                        ) VALUES (?, ?, ?, 'notification', ?, 'crm notification test', 1, 1, NOW())
                        """,
                43001L,
                "客户动态通知",
                "crm.customer.notification.test",
                "CRM通知"
        );
    }

    private void resetAuthorities() {
        jdbcTemplate.update("DELETE FROM user_authority_exclude WHERE camp_id = ? AND authority_id = 43001", CAMP_ID);
        jdbcTemplate.update("DELETE FROM authority_dict WHERE authority_id = 43001");
    }

    private int excludeCount() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM user_authority_exclude WHERE camp_id = ? AND user_id = ? AND authority_id = 43001 AND is_excluded = 1",
                Integer.class,
                CAMP_ID,
                USER_ID
        );
        return count == null ? 0 : count;
    }
}
