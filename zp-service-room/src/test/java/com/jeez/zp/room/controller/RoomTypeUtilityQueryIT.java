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
class RoomTypeUtilityQueryIT {

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
    void roomTypeFloorGet_shouldReturnActiveFloorsWithLinkedRoomTypes() throws Exception {
        insertRoomCategory(105001L, "TDD楼层房型A", 1);
        insertRoomCategory(105002L, "TDD楼层房型B", 2);
        insertFloor(105101L, "TDD楼层A", 11, 1, 0);
        insertFloor(105102L, "TDD楼层停用", 12, 0, 0);
        insertFloorRelation(105201L, 105001L, 105101L);
        insertFloorRelation(105202L, 105002L, 105101L);
        insertFloorRelation(105203L, 105001L, 105102L);

        mockMvc.perform(post("/roomType/floor/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","poiId":"11001","keyword":"TDD楼层"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.rows.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value("105101"))
                .andExpect(jsonPath("$.data.list[0].floorId").value("105101"))
                .andExpect(jsonPath("$.data.list[0].name").value("TDD楼层A"))
                .andExpect(jsonPath("$.data.list[0].floorName").value("TDD楼层A"))
                .andExpect(jsonPath("$.data.list[0].roomTypeCount").value(2))
                .andExpect(jsonPath("$.data.list[0].roomTypeIds[*]", hasItem("105001")))
                .andExpect(jsonPath("$.data.list[0].roomTypeIds[*]", hasItem("105002")))
                .andExpect(jsonPath("$.data.list[0].roomTypeNames[*]", hasItem("TDD楼层房型A")))
                .andExpect(jsonPath("$.data.roomTypeOptions[*].id", hasItem("105001")));
    }

    @Test
    @Timeout(60)
    void roomTypeTagGet_shouldReturnActiveTagsWithLinkedRoomTypes() throws Exception {
        insertRoomCategory(105011L, "TDD标签房型A", 1);
        insertRoomCategory(105012L, "TDD标签房型B", 2);
        insertTag(105301L, "TDD标签A", "facility", 21, 1, 0);
        insertTag(105302L, "TDD标签停用", "facility", 22, 0, 0);
        insertTagRelation(105401L, 105011L, 105301L);
        insertTagRelation(105402L, 105012L, 105301L);
        insertTagRelation(105403L, 105011L, 105302L);

        mockMvc.perform(post("/roomType/tag/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","keyword":"TDD标签"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.rows.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value("105301"))
                .andExpect(jsonPath("$.data.list[0].tagId").value("105301"))
                .andExpect(jsonPath("$.data.list[0].name").value("TDD标签A"))
                .andExpect(jsonPath("$.data.list[0].tagName").value("TDD标签A"))
                .andExpect(jsonPath("$.data.list[0].tagType").value("facility"))
                .andExpect(jsonPath("$.data.list[0].roomTypeCount").value(2))
                .andExpect(jsonPath("$.data.list[0].roomTypeIds[*]", hasItem("105011")))
                .andExpect(jsonPath("$.data.list[0].roomTypeNames[*]", hasItem("TDD标签房型B")))
                .andExpect(jsonPath("$.data.roomTypeOptions[*].id", hasItem("105011")));
    }

    @Test
    @Timeout(60)
    void roomTypeUtilityGet_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/roomType/floor/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10002"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301));
    }

    private void insertRoomCategory(long roomCategoryId, String name, int sortNo) {
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
                        ) VALUES (?, ?, ?, ?, 0, 1, ?, 0)
                        """,
                roomCategoryId,
                CAMP_ID,
                POI_ID,
                name,
                sortNo
        );
    }

    private void insertFloor(long floorId, String floorName, int sortNo, int status, int isDeleted) {
        jdbcTemplate.update("""
                        INSERT INTO room_floor (
                            floor_id,
                            camp_id,
                            poi_id,
                            floor_name,
                            sort_no,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                floorId,
                CAMP_ID,
                POI_ID,
                floorName,
                sortNo,
                status,
                isDeleted
        );
    }

    private void insertFloorRelation(long id, long roomCategoryId, long floorId) {
        jdbcTemplate.update("""
                        INSERT INTO room_category_floor_rel (
                            id,
                            camp_id,
                            room_category_id,
                            floor_id
                        ) VALUES (?, ?, ?, ?)
                        """,
                id,
                CAMP_ID,
                roomCategoryId,
                floorId
        );
    }

    private void insertTag(long tagId, String tagName, String tagType, int sortNo, int status, int isDeleted) {
        jdbcTemplate.update("""
                        INSERT INTO room_tag (
                            tag_id,
                            camp_id,
                            tag_name,
                            tag_type,
                            sort_no,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                tagId,
                CAMP_ID,
                tagName,
                tagType,
                sortNo,
                status,
                isDeleted
        );
    }

    private void insertTagRelation(long id, long roomCategoryId, long tagId) {
        jdbcTemplate.update("""
                        INSERT INTO room_category_tag_rel (
                            id,
                            camp_id,
                            room_category_id,
                            tag_id
                        ) VALUES (?, ?, ?, ?)
                        """,
                id,
                CAMP_ID,
                roomCategoryId,
                tagId
        );
    }
}
