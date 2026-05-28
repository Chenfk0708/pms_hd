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
class RoomCategoryGroupQueryIT {

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
    void roomCategoryGroupsGet_shouldReturnActiveGroupsWithDualShapes() throws Exception {
        insertGroup(21991L, "TDD房型分组A", 3, 1, 0);
        insertGroup(21992L, "TDD房型分组已停用", 4, 0, 0);
        insertGroup(21993L, "TDD房型分组已删除", 5, 1, 1);

        mockMvc.perform(post("/roomCategoryGroups/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.roomCategoryGroups.length()").value(3))
                .andExpect(jsonPath("$.data.list.length()").value(3))
                .andExpect(jsonPath("$.data.roomCategoryGroups[0].roomCategoryGroupId").value("21001"))
                .andExpect(jsonPath("$.data.roomCategoryGroups[0].roomCategoryGroupName").value("标准客房"))
                .andExpect(jsonPath("$.data.roomCategoryGroups[0].groupId").value("21001"))
                .andExpect(jsonPath("$.data.roomCategoryGroups[0].groupName").value("标准客房"))
                .andExpect(jsonPath("$.data.roomCategoryGroups[0].id").value("21001"))
                .andExpect(jsonPath("$.data.roomCategoryGroups[0].name").value("标准客房"))
                .andExpect(jsonPath("$.data.roomCategoryGroups[2].roomCategoryGroupId").value("21991"))
                .andExpect(jsonPath("$.data.roomCategoryGroups[2].roomCategoryGroupName").value("TDD房型分组A"))
                .andExpect(jsonPath("$.data.list[2].groupId").value("21991"))
                .andExpect(jsonPath("$.data.list[2].name").value("TDD房型分组A"));
    }

    @Test
    @Timeout(60)
    void roomCategoryGroupsGet_shouldFallbackCurrentCampWhenCampIdBlank() throws Exception {
        mockMvc.perform(post("/roomCategoryGroups/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":""}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roomCategoryGroups.length()").value(2))
                .andExpect(jsonPath("$.data.roomCategoryGroups[0].roomCategoryGroupId").value("21001"))
                .andExpect(jsonPath("$.data.list[1].groupId").value("21002"));
    }

    @Test
    @Timeout(60)
    void roomCategoryGroupsGet_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/roomCategoryGroups/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10002"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private void insertGroup(long groupId, String groupName, int sortNo, int status, int isDeleted) {
        jdbcTemplate.update("""
                        INSERT INTO room_category_group (
                            group_id,
                            camp_id,
                            poi_id,
                            group_name,
                            sort_no,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                groupId,
                CAMP_ID,
                POI_ID,
                groupName,
                sortNo,
                status,
                isDeleted
        );
    }
}
