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

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SystemMessageIT {

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
    void systemMessagePageAndReadActions_shouldUseRealReadTable() throws Exception {
        seedMessages();

        mockMvc.perform(post("/systemMessage/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "groupType":"crm"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list[0].messageId").value("42002"))
                .andExpect(jsonPath("$.data.list[0].title").value("CRM Message Read"))
                .andExpect(jsonPath("$.data.list[0].isRead").value(true))
                .andExpect(jsonPath("$.data.list[1].messageId").value("42001"))
                .andExpect(jsonPath("$.data.list[1].title").value("CRM Message Unread"))
                .andExpect(jsonPath("$.data.list[1].isRead").value(false));

        mockMvc.perform(post("/systemMessage/unReadCount/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "groupType":"crm"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(1));

        mockMvc.perform(post("/systemMessage/read/update")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "messageId":"42001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        assertThat(unreadCount()).isZero();

        mockMvc.perform(post("/systemMessage/read/all")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "groupType":"crm"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    private void seedMessages() {
        resetMessages();
        insertMessage(42001L, "CRM Message Unread", LocalDateTime.now().minusHours(2));
        insertMessage(42002L, "CRM Message Read", LocalDateTime.now().minusHours(1));
        jdbcTemplate.update("""
                        INSERT INTO system_message_read (
                            id,
                            message_id,
                            user_id,
                            is_read,
                            read_at,
                            created_at
                        ) VALUES (?, ?, ?, 1, NOW(), NOW())
                        """,
                42102L,
                42002L,
                USER_ID
        );
    }

    private void resetMessages() {
        jdbcTemplate.update("DELETE FROM system_message_read WHERE message_id BETWEEN 42001 AND 42002 OR id BETWEEN 42101 AND 42102");
        jdbcTemplate.update("DELETE FROM system_message WHERE camp_id = ? AND message_id BETWEEN 42001 AND 42002", CAMP_ID);
    }

    private void insertMessage(long messageId, String title, LocalDateTime createdAt) {
        jdbcTemplate.update("""
                        INSERT INTO system_message (
                            message_id,
                            camp_id,
                            group_type,
                            title,
                            content,
                            related_type,
                            related_id,
                            priority,
                            status,
                            created_at
                        ) VALUES (?, ?, 'crm', ?, ?, 'customer', 41001, 'normal', 1, ?)
                        """,
                messageId,
                CAMP_ID,
                title,
                title + " content",
                Timestamp.valueOf(createdAt)
        );
    }

    private int unreadCount() {
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM system_message sm
                        LEFT JOIN system_message_read smr ON smr.message_id = sm.message_id
                            AND smr.user_id = ?
                            AND smr.is_read = 1
                        WHERE sm.camp_id = ?
                          AND sm.message_id BETWEEN 42001 AND 42002
                          AND smr.id IS NULL
                        """,
                Integer.class,
                USER_ID,
                CAMP_ID
        );
        return count == null ? 0 : count;
    }
}
