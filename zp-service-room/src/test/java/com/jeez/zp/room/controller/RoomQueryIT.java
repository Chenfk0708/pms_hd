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
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoomQueryIT {

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
    void roomsGet_shouldReturnGroupedRoomsForRequestedCategories() throws Exception {
        insertRoomCategory(102501L, "TDD房型A", 10);
        insertRoomCategory(102502L, "TDD房型B", 20);
        insertFloor(102601L, "9F", 9);
        insertFloor(102602L, "10F", 10);
        insertRoom(102701L, 102501L, 102601L, "A-101", "online", "normal", "clean", 1, 1, 0);
        insertRoom(102702L, 102501L, 102601L, "A-102", "online", "normal", "dirty", 2, 1, 0);
        insertRoom(102703L, 102501L, 102602L, "A-103", "offline", "hourly", "dirty", 3, 1, 0);
        insertRoom(102704L, 102501L, 102602L, "A-104", "online", "normal", "clean", 4, 0, 0);
        insertRoom(102705L, 102502L, 102602L, "B-201", "online", "normal", "clean", 1, 1, 0);

        mockMvc.perform(post("/rooms/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "roomCategoryIds":["102502","102501"],
                                  "saleType":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.roomCategoryRooms.length()").value(2))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].roomCategoryId").value("102501"))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].roomCategoryName").value("TDD房型A"))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms.length()").value(2))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms[0].roomId").value("102701"))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms[0].roomName").value("A-101"))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms[0].floorId").value("102601"))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms[0].floorName").value("9F"))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms[0].seq").value(1))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms[0].deviceViews.length()").value(0))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms[*].roomId", not(hasItem("102703"))))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms[*].roomId", not(hasItem("102704"))))
                .andExpect(jsonPath("$.data.roomCategoryRooms[1].roomCategoryId").value("102502"))
                .andExpect(jsonPath("$.data.roomCategoryRooms[1].rooms[0].roomId").value("102705"));
    }

    @Test
    @Timeout(60)
    void roomsGet_shouldFallbackCurrentCampWhenCampIdBlank() throws Exception {
        insertRoomCategory(102801L, "TDD回退房型", 30);
        insertFloor(102811L, "11F", 11);
        insertRoom(102821L, 102801L, 102811L, "F-1101", "online", "normal", "clean", 1, 1, 0);

        mockMvc.perform(post("/rooms/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "roomCategoryIds":["102801"],
                                  "saleType":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roomCategoryRooms.length()").value(1))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].roomCategoryId").value("102801"))
                .andExpect(jsonPath("$.data.roomCategoryRooms[0].rooms[0].roomId").value("102821"));
    }

    @Test
    @Timeout(60)
    void roomsGet_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/rooms/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "roomCategoryIds":["102501"],
                                  "saleType":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private void insertRoomCategory(long roomCategoryId, String name, int sortNo) {
        jdbcTemplate.update("""
                        INSERT INTO room_category (
                            room_category_id,
                            camp_id,
                            poi_id,
                            name,
                            display_name,
                            room_count,
                            status,
                            sort_no,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                roomCategoryId,
                CAMP_ID,
                POI_ID,
                name,
                name,
                0,
                1,
                sortNo,
                0
        );
    }

    private void insertFloor(long floorId, String floorName, int sortNo) {
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
                1,
                0
        );
    }

    private void insertRoom(
            long roomId,
            long roomCategoryId,
            long floorId,
            String roomName,
            String lockStatus,
            String saleType,
            String cleanStatus,
            int sortNo,
            int status,
            int isDeleted
    ) {
        jdbcTemplate.update("""
                        INSERT INTO room (
                            room_id,
                            camp_id,
                            poi_id,
                            room_category_id,
                            floor_id,
                            room_name,
                            lock_status,
                            sale_type,
                            clean_status,
                            status,
                            sort_no,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                roomId,
                CAMP_ID,
                POI_ID,
                roomCategoryId,
                floorId,
                roomName,
                lockStatus,
                saleType,
                cleanStatus,
                status,
                sortNo,
                isDeleted
        );
    }
}
