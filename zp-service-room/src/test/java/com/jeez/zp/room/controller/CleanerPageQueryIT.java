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
class CleanerPageQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;
    private static final long POI_ID = 11001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void cleanerPageGet_shouldReturnPagedStaffDashboardFromRealTables() throws Exception {
        seedCleanerPageData();

        mockMvc.perform(post("/cleaner/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "poiId":"11001",
                                  "keyword":"Cleaner Page A",
                                  "status":"onDuty",
                                  "serviceDate":"2026-05-20",
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.requestBody.campId").value("10001"))
                .andExpect(jsonPath("$.data.requestBody.poiId").value("11001"))
                .andExpect(jsonPath("$.data.stores[*].id", hasItem("all")))
                .andExpect(jsonPath("$.data.stores[*].id", hasItem("11001")))
                .andExpect(jsonPath("$.data.summary.total").value(1))
                .andExpect(jsonPath("$.data.summary.onDuty").value(1))
                .andExpect(jsonPath("$.data.summary.todayTasks").value(2))
                .andExpect(jsonPath("$.data.summary.completedTasks").value(1))
                .andExpect(jsonPath("$.data.pagination.page").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(10))
                .andExpect(jsonPath("$.data.pagination.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].cleanerId").value("106101"))
                .andExpect(jsonPath("$.data.list[0].id").value("106101"))
                .andExpect(jsonPath("$.data.list[0].cleanerName").value("Cleaner Page A"))
                .andExpect(jsonPath("$.data.list[0].name").value("Cleaner Page A"))
                .andExpect(jsonPath("$.data.list[0].mobile").value("13800006001"))
                .andExpect(jsonPath("$.data.list[0].poiId").value("11001"))
                .andExpect(jsonPath("$.data.list[0].workStatus").value("onDuty"))
                .andExpect(jsonPath("$.data.list[0].status").value("onDuty"))
                .andExpect(jsonPath("$.data.list[0].roleName").value("保洁员"))
                .andExpect(jsonPath("$.data.list[0].roomScopes[*]", hasItem("Cleaner Page RoomType")))
                .andExpect(jsonPath("$.data.list[0].todayTaskNum").value(2))
                .andExpect(jsonPath("$.data.list[0].completedTaskNum").value(1))
                .andExpect(jsonPath("$.data.list[0].lastTaskTime").value("2026-05-20 15:00"));
    }

    @Test
    @Timeout(60)
    void cleanerPageGet_shouldSupportOffDutyFilter() throws Exception {
        seedCleanerPageData();

        mockMvc.perform(post("/cleaner/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "status":"offDuty",
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.summary.total").value(1))
                .andExpect(jsonPath("$.data.summary.offDuty").value(1))
                .andExpect(jsonPath("$.data.list[0].cleanerId").value("106102"))
                .andExpect(jsonPath("$.data.list[0].workStatus").value("offDuty"));
    }

    @Test
    @Timeout(60)
    void cleanerPageGet_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/cleaner/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10002"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301));
    }

    @Test
    @Timeout(60)
    void cleanerSaveAndExport_shouldPersistAndReturnRealRows() throws Exception {
        seedCleanerPageData();

        mockMvc.perform(post("/cleaner/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "name":"Cleaner Save API",
                                  "mobile":"13900009999",
                                  "roomScopeText":"Cleaner Save Scope",
                                  "status":"onDuty"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.saved").value(true))
                .andExpect(jsonPath("$.data.cleanerId").isString());

        mockMvc.perform(post("/cleaner/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "keyword":"Cleaner Save API",
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].cleanerName").value("Cleaner Save API"))
                .andExpect(jsonPath("$.data.list[0].mobile").value("13900009999"));

        mockMvc.perform(post("/cleaner/export")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "keyword":"Cleaner Save API",
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.fileName").value("cleaners_" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE) + ".csv"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.rows[0].cleanerName").value("Cleaner Save API"));
    }

    private void seedCleanerPageData() {
        insertRoomCategory(106001L, "Cleaner Page RoomType");
        insertRoom(106011L, "Cleaner Page Room");
        insertCleanStaff(106101L, "Cleaner Page A", "13800006001", 1, 0);
        insertCleanStaff(106102L, "Cleaner Page B", "13800006002", 0, 0);
        insertCleanStaff(106103L, "Cleaner Page Deleted", "13800006003", 1, 1);
        insertCleanTask(106201L, 106101L, "checkout_clean", "done", "2026-05-20 11:00:00");
        insertCleanTask(106202L, 106101L, "daily_clean", "pending", "2026-05-20 15:00:00");
        insertCleanTask(106203L, 106102L, "daily_clean", "pending", "2026-05-21 10:00:00");
    }

    private void insertRoomCategory(long roomCategoryId, String name) {
        jdbcTemplate.update("""
                        INSERT INTO room_category (
                            room_category_id,
                            camp_id,
                            poi_id,
                            name,
                            room_count,
                            status,
                            sort_no,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, 1, 1, 1, 0)
                        """,
                roomCategoryId,
                CAMP_ID,
                POI_ID,
                name
        );
    }

    private void insertRoom(long roomId, String roomName) {
        jdbcTemplate.update("""
                        INSERT INTO room (
                            room_id,
                            camp_id,
                            poi_id,
                            room_category_id,
                            room_name,
                            room_no,
                            status,
                            sort_no,
                            is_deleted
                        ) VALUES (?, ?, ?, 106001, ?, ?, 1, 1, 0)
                        """,
                roomId,
                CAMP_ID,
                POI_ID,
                roomName,
                roomName
        );
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
                        ) VALUES (?, ?, ?, ?, ?, 'cleaner-page-it', ?)
                        """,
                cleanStaffId,
                CAMP_ID,
                name,
                mobile,
                status,
                isDeleted
        );
    }

    private void insertCleanTask(long taskId, long cleanStaffId, String taskType, String taskStatus, String deadlineAt) {
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
                            remark
                        ) VALUES (?, ?, ?, 106011, 106001, ?, ?, ?, ?, 'cleaner-page-it')
                        """,
                taskId,
                CAMP_ID,
                POI_ID,
                cleanStaffId,
                taskType,
                taskStatus,
                deadlineAt
        );
    }
}
