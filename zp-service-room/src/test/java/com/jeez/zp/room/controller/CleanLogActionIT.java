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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CleanLogActionIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void cleanLogPageGet_shouldReturnLegacyFrontendFieldsAndSupportFilters() throws Exception {
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
                                  "operatorId":"130101",
                                  "operatorStartTime":1778688000000,
                                  "operatorEndTime":1778774400000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value("130201"))
                .andExpect(jsonPath("$.data.list[0].operatorTime").value("2026-05-14 09:18:26"))
                .andExpect(jsonPath("$.data.list[0].operatorName").value("Log Cleaner A"))
                .andExpect(jsonPath("$.data.list[0].operatorTypeName").value("退房保洁"))
                .andExpect(jsonPath("$.data.list[0].operatorDetails").value("102 checkout clean completed"))
                .andExpect(jsonPath("$.data.list[0].roomType").value("标准大床房"))
                .andExpect(jsonPath("$.data.list[0].roomName").value(RoomCleanTestCatalogFixture.ROOM_102_NAME));
    }

    @Test
    @Timeout(60)
    void cleanManageCleanLogList_shouldShareTheSameRealQueryContract() throws Exception {
        seedCleanLogs();

        mockMvc.perform(post("/cleanManage/cleanLog/list")
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
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list[0].id").value("130202"))
                .andExpect(jsonPath("$.data.list[1].id").value("130201"));
    }

    @Test
    @Timeout(60)
    void cleanManageCleanLogExport_shouldReturnCsvMetadataAndFilteredRows() throws Exception {
        seedCleanLogs();

        mockMvc.perform(post("/cleanManage/cleanLog/export")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "roomId":["23002"],
                                  "operatorStartTime":1778688000000,
                                  "operatorEndTime":1778774400000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.fileName").value("clean_logs_2026-05-14.csv"))
                .andExpect(jsonPath("$.data.contentType").value("text/csv"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.rows[0].id").value("130201"));
    }

    @Test
    @Timeout(60)
    void cleanLogPageGet_shouldRejectForeignCampAccess() throws Exception {
        seedCleanLogs();

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
        RoomCleanTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        insertCleanStaff(130101L, "Log Cleaner A", "13800003001");
        insertCleanStaff(130102L, "Log Cleaner B", "13800003002");
        insertCleanTask(130201L, 11001L, 23002L, 22001L, 130101L, "checkout_clean", "done", "102 checkout clean completed", "2026-05-14 09:18:26");
        insertCleanTask(130202L, 11001L, 23005L, 22002L, 130102L, "daily_clean", "processing", "203 daily clean processing", "2026-05-15 10:04:12");
    }

    private void resetCleanLogs() {
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
                "clean-log-action-it",
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
