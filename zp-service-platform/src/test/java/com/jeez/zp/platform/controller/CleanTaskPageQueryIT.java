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

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CleanTaskPageQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void cleanTaskPageGet_shouldReturnCurrentFrontendDashboardAndSupportFilters() throws Exception {
        seedCleanTasks();

        mockMvc.perform(post("/cleanTask/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "poiId":"11001",
                                  "cleanTime":"2026-05-18",
                                  "roomId":"23002",
                                  "cleanType":"CHECKOUT",
                                  "cleanStatus":"PENDING_CLEAN",
                                  "cleanerIds":["98101"],
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.summary.total").value(1))
                .andExpect(jsonPath("$.data.summary.pendingAssign").value(0))
                .andExpect(jsonPath("$.data.summary.pendingClean").value(1))
                .andExpect(jsonPath("$.data.summary.cleaning").value(0))
                .andExpect(jsonPath("$.data.summary.done").value(0))
                .andExpect(jsonPath("$.data.pagination.page").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(20))
                .andExpect(jsonPath("$.data.pagination.total").value(1))
                .andExpect(jsonPath("$.data.stores[0].id").value("ALL"))
                .andExpect(jsonPath("$.data.stores[1].id").value("11001"))
                .andExpect(jsonPath("$.data.stores[1].label").value("路客云演示门店"))
                .andExpect(jsonPath("$.data.cleaners[?(@.id=='98101')].label").value(hasItem("保洁任务A")))
                .andExpect(jsonPath("$.data.rooms[?(@.id=='23002')].label").value(hasItem("标准大床房 / 102")))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].taskId").value("98201"))
                .andExpect(jsonPath("$.data.list[0].taskNo").value("CT98201"))
                .andExpect(jsonPath("$.data.list[0].roomName").value("标准大床房 / 102"))
                .andExpect(jsonPath("$.data.list[0].poiName").value("路客云演示门店"))
                .andExpect(jsonPath("$.data.list[0].cleanType").value("CHECKOUT"))
                .andExpect(jsonPath("$.data.list[0].cleanStatus").value("PENDING_CLEAN"))
                .andExpect(jsonPath("$.data.list[0].cleanerId").value("98101"))
                .andExpect(jsonPath("$.data.list[0].cleanerName").value("保洁任务A"))
                .andExpect(jsonPath("$.data.list[0].cleanDate").value("2026-05-18"))
                .andExpect(jsonPath("$.data.list[0].planTime").value("14:00"))
                .andExpect(jsonPath("$.data.list[0].deadline").value("14:00"))
                .andExpect(jsonPath("$.data.list[0].remark").value("102 退房待保洁"))
                .andExpect(jsonPath("$.data.list[0].progress").value(20))
                .andExpect(jsonPath("$.data.list[0].priority").value("urgent"));
    }

    @Test
    @Timeout(60)
    void cleanTaskPageGet_shouldFallbackCurrentCampAndRejectForeignCampAccess() throws Exception {
        seedCleanTasks();

        mockMvc.perform(post("/cleanTask/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "cleanTime":"2026-05-18",
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.summary.total").value(3))
                .andExpect(jsonPath("$.data.summary.pendingAssign").value(1))
                .andExpect(jsonPath("$.data.summary.pendingClean").value(1))
                .andExpect(jsonPath("$.data.summary.cleaning").value(1))
                .andExpect(jsonPath("$.data.summary.done").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(3));

        mockMvc.perform(post("/cleanTask/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "cleanTime":"2026-05-18",
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.message").value("无权访问当前门店保洁任务"));
    }

    private void seedCleanTasks() {
        resetCleanTasks();
        insertCleanStaff(98101L, "保洁任务A", "13800001001");
        insertCleanStaff(98102L, "保洁任务B", "13800001002");
        insertCleanTask(98201L, 11001L, 23002L, 22001L, 98101L, "checkout_clean", "pending", "102 退房待保洁", "2026-05-18 14:00:00");
        insertCleanTask(98202L, 11001L, 23005L, 22002L, 98102L, "daily_clean", "processing", "203 续住保洁中", "2026-05-18 16:30:00");
        insertCleanTask(98203L, 11001L, 23003L, 22001L, null, "deep_clean", "pending", "201 计划深度保洁", "2026-05-18 09:00:00");
        insertCleanTask(98204L, 11001L, 23002L, 22001L, 98101L, "checkout_clean", "done", "102 昨日退房保洁完成", "2026-05-17 12:00:00");
    }

    private void resetCleanTasks() {
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
                "integration-test",
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
                0,
                remark,
                deadlineAt,
                deadlineAt
        );
    }
}
