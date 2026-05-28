package com.jeez.zp.room.controller;

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

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CleanerListQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void cleanerListGet_shouldFallbackCurrentCampAndReturnEnabledCleaners() throws Exception {
        seedCleaners();

        mockMvc.perform(post("/cleaner/list/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[*].cleanerId", hasItem("129101")))
                .andExpect(jsonPath("$.data[*].cleanerName", hasItem("Room Cleaner List A")));
    }

    @Test
    @Timeout(60)
    void cleanerListGet_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/cleaner/list/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    private void seedCleaners() {
        resetCleaners();
        insertCleanStaff(129101L, "Room Cleaner List A", "13800002001", 1, 0);
        insertCleanStaff(129102L, "Room Cleaner List B", "13800002002", 1, 0);
        insertCleanStaff(129103L, "Room Cleaner Disabled", "13800002003", 0, 0);
        insertCleanStaff(129104L, "Room Cleaner Deleted", "13800002004", 1, 1);
    }

    private void resetCleaners() {
        jdbcTemplate.update("""
                DELETE FROM clean_task
                WHERE camp_id = ?
                  AND clean_staff_id IN (129101, 129102, 129103, 129104)
                """, CAMP_ID);
        jdbcTemplate.update("""
                DELETE FROM clean_staff
                WHERE camp_id = ?
                  AND clean_staff_id IN (129101, 129102, 129103, 129104)
                """, CAMP_ID);
    }

    private void insertCleanStaff(long cleanStaffId, String name, String mobile, int status, int isDeleted) {
        jdbcTemplate.update("""
                        INSERT INTO clean_staff (
                            clean_staff_id,
                            camp_id,
                            name,
                            mobile,
                            status,
                            remark,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                cleanStaffId,
                CAMP_ID,
                name,
                mobile,
                status,
                "integration-test",
                isDeleted
        );
    }
}
