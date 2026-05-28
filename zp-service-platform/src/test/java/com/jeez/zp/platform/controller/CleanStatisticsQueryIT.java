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
class CleanStatisticsQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void cleanTaskStatistics_shouldReturnCurrentFrontendSummaryRowsAndSupportFilters() throws Exception {
        seedCleanStatisticsData();

        mockMvc.perform(post("/cleanTask/statistics")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "storeId":"11001",
                                  "roomIds":["23002"],
                                  "cleanerIds":["99101"],
                                  "cleanStartTime":1778947200000,
                                  "cleanEndTime":1779033599999,
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].cleanTime").value("合计"))
                .andExpect(jsonPath("$.data.list[0].countNum").value(1))
                .andExpect(jsonPath("$.data.list[0].countCost").value(6600))
                .andExpect(jsonPath("$.data.list[0].cleanTypeThreeNum").value(1))
                .andExpect(jsonPath("$.data.list[0].cleanTypeThreeCost").value(6600))
                .andExpect(jsonPath("$.data.list[1].cleanTime").value("2026-05-17"))
                .andExpect(jsonPath("$.data.list[1].countNum").value(1))
                .andExpect(jsonPath("$.data.list[1].cleanTypeThreeNum").value(1));
    }

    @Test
    @Timeout(60)
    void cleanerListGet_shouldFallbackCurrentCampAndRejectForeignCampAccess() throws Exception {
        seedCleanStatisticsData();

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
                .andExpect(jsonPath("$.data[*].cleanerId", hasItem("99101")))
                .andExpect(jsonPath("$.data[*].cleanerName", hasItem("统计保洁A")));

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
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").value("无权访问当前门店保洁统计"));
    }

    private void seedCleanStatisticsData() {
        resetCleanStatisticsData();
        insertCleanStaff(99101L, "统计保洁A", "13800002001");
        insertCleanStaff(99102L, "统计保洁B", "13800002002");
        insertCleanTask(99201L, 11001L, 23002L, 22001L, 99101L, "checkout_clean", "done", "102 退房保洁完成", "2026-05-17 14:00:00");
        insertCleanTask(99202L, 11001L, 23005L, 22002L, 99102L, "daily_clean", "done", "203 续住保洁完成", "2026-05-17 16:30:00");
        insertCleanTask(99203L, 11001L, 23003L, 22001L, null, "deep_clean", "pending", "201 深度保洁待分配", "2026-05-18 09:00:00");
    }

    private void resetCleanStatisticsData() {
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
