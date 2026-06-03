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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CleanTaskActionIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void cleanTaskCreate_shouldInsertTaskFromRealRoomAndCleaner() throws Exception {
        resetCleanData();
        RoomCleanTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        insertCleanStaff(128301L, "Action Cleaner A", "13800001301");

        mockMvc.perform(post("/cleanTask/create")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "poiId":"11001",
                                  "roomId":"23002",
                                  "cleanerId":"128301",
                                  "cleanType":"CHECKOUT",
                                  "cleanStatus":"PENDING_CLEAN",
                                  "deadlineAt":"2026-05-19 15:30:00",
                                  "remark":"created by action it"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.taskId", not(blankOrNullString())))
                .andExpect(jsonPath("$.data.taskNo", containsString("CT")))
                .andExpect(jsonPath("$.data.cleanStatus").value("PENDING_CLEAN"))
                .andExpect(jsonPath("$.data.message").value("保洁任务创建成功"));

        Integer created = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM clean_task
                        WHERE camp_id = ?
                          AND poi_id = 11001
                          AND room_id = 23002
                          AND room_category_id = 22001
                          AND clean_staff_id = 128301
                          AND task_type = 'checkout_clean'
                          AND task_status = 'pending'
                          AND deadline_at = '2026-05-19 15:30:00'
                          AND notify_count = 0
                          AND remark = 'created by action it'
                        """,
                Integer.class,
                CAMP_ID
        );
        assertEquals(1, created);
    }

    @Test
    @Timeout(60)
    void cleanTaskNotify_shouldIncrementNotifyCountForAccessibleTasks() throws Exception {
        resetCleanData();
        RoomCleanTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        insertCleanStaff(128301L, "Action Cleaner A", "13800001301");
        insertCleanTask(128302L, 11001L, 23002L, 22001L, 128301L, "checkout_clean", "pending", 1, "notify target", "2026-05-19 10:00:00");

        mockMvc.perform(post("/cleanTask/notify")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "taskIds":["128302"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.notifiedCount").value(1))
                .andExpect(jsonPath("$.data.taskIds[0]").value("128302"))
                .andExpect(jsonPath("$.data.message").value("保洁任务通知成功"));

        Integer notifyCount = jdbcTemplate.queryForObject(
                "SELECT notify_count FROM clean_task WHERE clean_task_id = ?",
                Integer.class,
                128302L
        );
        assertEquals(2, notifyCount);
    }

    @Test
    @Timeout(60)
    void cleanTaskExport_shouldReturnCsvRowsFromRealFilteredTasks() throws Exception {
        resetCleanData();
        RoomCleanTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        insertCleanStaff(128301L, "Action Cleaner A", "13800001301");
        insertCleanTask(128303L, 11001L, 23002L, 22001L, 128301L, "checkout_clean", "pending", 0, "export target A", "2026-05-19 10:00:00");
        insertCleanTask(128304L, 11001L, 23003L, 22001L, null, "deep_clean", "pending", 0, "export target B", "2026-05-19 11:00:00");

        mockMvc.perform(post("/cleanTask/export")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "poiId":"11001",
                                  "cleanTime":"2026-05-19",
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.fileName").value("clean_tasks_2026-05-19.csv"))
                .andExpect(jsonPath("$.data.contentType").value("text/csv"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.rows.length()").value(2))
                .andExpect(jsonPath("$.data.rows[0].taskId").value("128304"))
                .andExpect(jsonPath("$.data.rows[0].remark").value("export target B"))
                .andExpect(jsonPath("$.data.rows[1].taskId").value("128303"))
                .andExpect(jsonPath("$.data.rows[1].remark").value("export target A"));
    }

    private void resetCleanData() {
        jdbcTemplate.update("DELETE FROM clean_task WHERE camp_id = ?", CAMP_ID);
        jdbcTemplate.update("DELETE FROM clean_staff WHERE camp_id = ?", CAMP_ID);
    }

    private void insertCleanStaff(long cleanStaffId, String name, String mobile) {
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
                1,
                "clean-task-action-it",
                0
        );
    }

    private void insertCleanTask(
            long cleanTaskId,
            long poiId,
            long roomId,
            long roomCategoryId,
            Long cleanStaffId,
            String taskType,
            String taskStatus,
            int notifyCount,
            String remark,
            String deadlineAt
    ) {
        jdbcTemplate.update("""
                        INSERT INTO clean_task (
                            clean_task_id,
                            camp_id,
                            poi_id,
                            room_id,
                            room_category_id,
                            clean_staff_id,
                            task_type,
                            task_status,
                            deadline_at,
                            notify_count,
                            remark,
                            created_at,
                            updated_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                cleanTaskId,
                CAMP_ID,
                poiId,
                roomId,
                roomCategoryId,
                cleanStaffId,
                taskType,
                taskStatus,
                deadlineAt,
                notifyCount,
                remark,
                deadlineAt,
                deadlineAt
        );
    }
}
