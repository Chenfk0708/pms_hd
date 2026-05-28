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
class CleanLogQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void cleanLogPageGet_shouldReturnCurrentFrontendFieldsAndSupportFilters() throws Exception {
        seedCleanLogs();

        mockMvc.perform(post("/cleanLog/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "roomId":["23002"],
                                  "operatorId":"97101",
                                  "operatorStartTime":1778688000000,
                                  "operatorEndTime":1778774400000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value("97201"))
                .andExpect(jsonPath("$.data.list[0].operatorTime").value("2026-05-14 09:18:26"))
                .andExpect(jsonPath("$.data.list[0].operatorName").value("保洁测试A"))
                .andExpect(jsonPath("$.data.list[0].operatorTypeName").value("退房保洁"))
                .andExpect(jsonPath("$.data.list[0].operatorDetails").value("102 退房保洁完成"))
                .andExpect(jsonPath("$.data.list[0].roomType").value("标准大床房"))
                .andExpect(jsonPath("$.data.list[0].roomName").value("102"));
    }

    @Test
    @Timeout(60)
    void cleanLogPageGet_shouldFallbackCurrentCampAndRejectForeignCampAccess() throws Exception {
        seedCleanLogs();

        mockMvc.perform(post("/cleanLog/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(2));

        mockMvc.perform(post("/cleanLog/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").value("无权访问当前门店保洁日志"));
    }

    private void seedCleanLogs() {
        resetCleanLogs();
        insertCleanStaff(97101L, "保洁测试A", "13800000001");
        insertCleanStaff(97102L, "保洁测试B", "13800000002");
        insertCleanTask(97201L, 11001L, 23002L, 22001L, 97101L, "checkout_clean", "done", "102 退房保洁完成", "2026-05-14 09:18:26");
        insertCleanTask(97202L, 11001L, 23005L, 22002L, 97102L, "daily_clean", "processing", "203 住中保洁进行中", "2026-05-15 10:04:12");
    }

    private void resetCleanLogs() {
        jdbcTemplate.update("DELETE FROM clean_task WHERE camp_id = ?", CAMP_ID);
        jdbcTemplate.update("DELETE FROM clean_staff WHERE clean_staff_id IN (?, ?)", 97101L, 97102L);
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
            long cleanStaffId,
            String taskType,
            String taskStatus,
            String remark,
            String updatedAt
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
                updatedAt,
                0,
                remark,
                updatedAt,
                updatedAt
        );
    }
}
