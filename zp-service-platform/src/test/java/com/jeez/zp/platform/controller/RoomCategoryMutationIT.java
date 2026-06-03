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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoomCategoryMutationIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;
    private static final long USER_ID = 12001L;
    private static final long POI_ID = 11001L;
    private static final long GROUP_ID = 21001L;
    private static final long ROOM_CATEGORY_A = 92901L;
    private static final long ROOM_CATEGORY_B = 92902L;
    private static final long ROOM_CATEGORY_C = 92903L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void roomCategoryLinkageGetAndSave_shouldUseRoomCategoryLinkageTable() throws Exception {
        insertRoomCategory(ROOM_CATEGORY_A, "TDD联动房型A", 1, 10);
        insertRoomCategory(ROOM_CATEGORY_B, "TDD联动房型B", 1, 20);
        insertRoomCategory(ROOM_CATEGORY_C, "TDD联动房型C", 1, 30);
        insertLinkage(92911L, ROOM_CATEGORY_A, ROOM_CATEGORY_B, 1);

        mockMvc.perform(post("/roomCategory/linkage/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","roomCategoryId":"92901"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.roomTypeId").value("92901"))
                .andExpect(jsonPath("$.data.roomTypeName").value("TDD联动房型A"))
                .andExpect(jsonPath("$.data.candidates[?(@.id == '92902')].selected").value(true))
                .andExpect(jsonPath("$.data.candidates[?(@.id == '92903')].selected").value(false));

        mockMvc.perform(post("/roomCategory/linkage/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "roomCategoryId":"92901",
                                  "linkedRoomCategoryIds":["92902","92903"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.message").value("联动关房已更新"));

        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM room_category_linkage
                WHERE camp_id = ? AND room_category_id = ? AND status = 1
                """, Integer.class, CAMP_ID, ROOM_CATEGORY_A)).isEqualTo(2);
    }

    @Test
    @Timeout(60)
    void roomCategoryDetailGetAndSave_shouldUpdateRoomCategoryAndRooms() throws Exception {
        insertRoomCategory(ROOM_CATEGORY_A, "TDD保存房型A", 1, 10);
        insertRoom(92921L, ROOM_CATEGORY_A, "A-101", 1);

        mockMvc.perform(post("/roomCategory/detail/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","roomCategoryId":"92901","mode":"detail"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.mode").value("detail"))
                .andExpect(jsonPath("$.data.title").value("房型详情"))
                .andExpect(jsonPath("$.data.form.roomTypeId").value("92901"))
                .andExpect(jsonPath("$.data.form.roomTypeName").value("TDD保存房型A"))
                .andExpect(jsonPath("$.data.form.roomNos[0]").value("A-101"));

        mockMvc.perform(post("/roomCategory/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "form":{
                                    "roomTypeId":"92901",
                                    "roomTypeName":"TDD保存房型A改",
                                    "storeId":"11001",
                                    "groupId":"21001",
                                    "roomCount":"2",
                                    "roomNos":["A-201","A-202"],
                                    "weekdayPrice":"268",
                                    "weekendPrice":"288",
                                    "holidayPrice":"308",
                                    "rentalType":"entire",
                                    "propertyType":"apartment",
                                    "guestCount":"2",
                                    "displayName":"TDD展示名",
                                    "earliestCheckIn":"14",
                                    "latestCheckIn":"23",
                                    "latestCheckOut":"12",
                                    "highlightDescription":"亮点",
                                    "nearbyDescription":"周边",
                                    "articleDescription":"图文"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.message").value("房型信息已保存"));

        assertThat(jdbcTemplate.queryForObject("""
                SELECT name
                FROM room_category
                WHERE room_category_id = ?
                """, String.class, ROOM_CATEGORY_A)).isEqualTo("TDD保存房型A改");
        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM room
                WHERE room_category_id = ? AND is_deleted = 0
                """, Integer.class, ROOM_CATEGORY_A)).isEqualTo(2);
    }

    @Test
    @Timeout(60)
    void roomCategorySave_shouldPersistPhotosAndReturnThemInEditDetail() throws Exception {
        long mediaResourceId = 92951L;
        String mediaUrl = "https://assets.localhome.cn/room-type/save-cover.png";
        insertMedia(mediaResourceId, mediaUrl);

        mockMvc.perform(post("/roomCategory/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "form":{
                                    "roomTypeName":"TDD照片保存房型",
                                    "storeId":"11001",
                                    "groupId":"21001",
                                    "roomCount":"1",
                                    "roomNos":["P-101"],
                                    "weekdayPrice":"268",
                                    "weekendPrice":"288",
                                    "holidayPrice":"308",
                                    "rentalType":"entire",
                                    "propertyType":"apartment",
                                    "guestCount":"2",
                                    "displayName":"TDD照片展示名",
                                    "earliestCheckIn":"14",
                                    "latestCheckIn":"23",
                                    "latestCheckOut":"12",
                                    "photos":[
                                      {
                                        "id":"92951",
                                        "mediaResourceId":"92951",
                                        "sectionKey":"cover",
                                        "name":"save-cover.png",
                                        "url":"https://assets.localhome.cn/room-type/save-cover.png",
                                        "size":4,
                                        "mimeType":"image/png",
                                        "sortOrder":1
                                      }
                                    ]
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roomCategoryId").exists())
                .andExpect(jsonPath("$.data.message").value("房型已创建"));

        Long roomCategoryId = jdbcTemplate.queryForObject("""
                SELECT room_category_id
                FROM room_category
                WHERE camp_id = ? AND name = ? AND is_deleted = 0
                ORDER BY created_at DESC
                LIMIT 1
                """, Long.class, CAMP_ID, "TDD照片保存房型");

        assertThat(roomCategoryId).isNotNull();
        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM room_category_media
                WHERE camp_id = ?
                  AND room_category_id = ?
                  AND media_resource_id = ?
                  AND media_url = ?
                  AND scene_type = 'cover'
                  AND status = 1
                """, Integer.class, CAMP_ID, roomCategoryId, mediaResourceId, mediaUrl)).isEqualTo(1);

        mockMvc.perform(post("/roomCategory/detail/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","roomCategoryId":"%s","mode":"detail"}
                                """.formatted(roomCategoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.form.photos.length()").value(1))
                .andExpect(jsonPath("$.data.form.photos[0].id").value("92951"))
                .andExpect(jsonPath("$.data.form.photos[0].sectionKey").value("cover"))
                .andExpect(jsonPath("$.data.form.photos[0].url").value(mediaUrl))
                .andExpect(jsonPath("$.data.form.photoCounts.cover").value(1));
    }

    @Test
    @Timeout(60)
    void roomCategorySave_shouldPersistLocationAndFacilityFormFields() throws Exception {
        mockMvc.perform(post("/roomCategory/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "form":{
                                    "roomTypeName":"TDD完整字段房型",
                                    "storeId":"11001",
                                    "groupId":"21001",
                                    "roomCount":"1",
                                    "roomNos":["F-101"],
                                    "weekdayPrice":"268",
                                    "weekendPrice":"288",
                                    "holidayPrice":"308",
                                    "locationMode":"independent",
                                    "locationProvinceCode":"440000",
                                    "locationProvinceName":"Guangdong",
                                    "locationCityCode":"440300",
                                    "locationCityName":"Shenzhen",
                                    "locationDistrictCode":"440309",
                                    "locationDistrictName":"Longhua",
                                    "streetAddress":"Minzhi Street",
                                    "communityName":"Yinsu Community",
                                    "buildingUnit":"Building 1 Unit 2",
                                    "doorNumber":"1808",
                                    "locationLatitude":"22.543096",
                                    "locationLongitude":"114.057865",
                                    "rentalType":"entire",
                                    "propertyType":"apartment",
                                    "suiteArea":"68.5",
                                    "guestCount":"2",
                                    "bedroomCount":"1",
                                    "livingRoomCount":"1",
                                    "kitchenCount":"1",
                                    "bathroomCount":"1",
                                    "bathroomType":"private",
                                    "selectedFacilityIds":["wifi","self-checkin","white-bedding"],
                                    "bedSheetChangePolicy":"one-guest-one-change",
                                    "decorationStyle":"modern",
                                    "displayName":"TDD完整字段展示名",
                                    "earliestCheckIn":"14",
                                    "latestCheckIn":"23",
                                    "latestCheckOut":"12"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roomCategoryId").exists());

        Long roomCategoryId = jdbcTemplate.queryForObject("""
                SELECT room_category_id
                FROM room_category
                WHERE camp_id = ? AND name = ? AND is_deleted = 0
                ORDER BY created_at DESC
                LIMIT 1
                """, Long.class, CAMP_ID, "TDD完整字段房型");

        mockMvc.perform(post("/roomCategory/detail/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","roomCategoryId":"%s","mode":"detail"}
                                """.formatted(roomCategoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.form.locationMode").value("independent"))
                .andExpect(jsonPath("$.data.form.locationProvinceCode").value("440000"))
                .andExpect(jsonPath("$.data.form.locationProvinceName").value("Guangdong"))
                .andExpect(jsonPath("$.data.form.locationCityCode").value("440300"))
                .andExpect(jsonPath("$.data.form.locationCityName").value("Shenzhen"))
                .andExpect(jsonPath("$.data.form.locationDistrictCode").value("440309"))
                .andExpect(jsonPath("$.data.form.locationDistrictName").value("Longhua"))
                .andExpect(jsonPath("$.data.form.streetAddress").value("Minzhi Street"))
                .andExpect(jsonPath("$.data.form.communityName").value("Yinsu Community"))
                .andExpect(jsonPath("$.data.form.buildingUnit").value("Building 1 Unit 2"))
                .andExpect(jsonPath("$.data.form.doorNumber").value("1808"))
                .andExpect(jsonPath("$.data.form.locationLatitude").value("22.543096"))
                .andExpect(jsonPath("$.data.form.locationLongitude").value("114.057865"))
                .andExpect(jsonPath("$.data.form.selectedFacilityIds[0]").value("wifi"))
                .andExpect(jsonPath("$.data.form.selectedFacilityIds[1]").value("self-checkin"))
                .andExpect(jsonPath("$.data.form.selectedFacilityIds[2]").value("white-bedding"))
                .andExpect(jsonPath("$.data.form.bedSheetChangePolicy").value("one-guest-one-change"))
                .andExpect(jsonPath("$.data.form.decorationStyle").value("modern"));
    }

    @Test
    @Timeout(60)
    void roomCategorySave_shouldReturnBusinessErrorWhenRoomNoAlreadyExistsInStore() throws Exception {
        insertRoomCategory(ROOM_CATEGORY_A, "TDD已有房间号房型", 1, 10);
        insertRoom(92971L, ROOM_CATEGORY_A, "TDD-CONFLICT-101", 1);

        mockMvc.perform(post("/roomCategory/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "form":{
                                    "roomTypeName":"TDD重复房间号房型",
                                    "storeId":"11001",
                                    "groupId":"21001",
                                    "roomCount":"1",
                                    "roomNos":["TDD-CONFLICT-101"],
                                    "weekdayPrice":"268",
                                    "weekendPrice":"288",
                                    "holidayPrice":"308",
                                    "rentalType":"entire",
                                    "propertyType":"apartment",
                                    "guestCount":"2",
                                    "displayName":"TDD重复房间号房型",
                                    "earliestCheckIn":"14",
                                    "latestCheckIn":"23",
                                    "latestCheckOut":"12"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.errorMsg").value("房间号已存在：TDD-CONFLICT-101"));
    }

    @Test
    @Timeout(60)
    void roomCategoryDelete_shouldSoftDeleteCategoryRoomsAndPendingCleanTasks() throws Exception {
        insertRoomCategory(ROOM_CATEGORY_A, "TDD删除房型A", 1, 10);
        insertRoom(92931L, ROOM_CATEGORY_A, "D-101", 1);
        insertCleanTask(92941L, 92931L, ROOM_CATEGORY_A);

        mockMvc.perform(post("/roomCategory/delete")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","roomCategoryId":"92901"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.message").value("房型已删除"));

        assertThat(jdbcTemplate.queryForObject("""
                SELECT is_deleted
                FROM room_category
                WHERE room_category_id = ?
                """, Integer.class, ROOM_CATEGORY_A)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("""
                SELECT is_deleted
                FROM room
                WHERE room_id = ?
                """, Integer.class, 92931L)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM clean_task
                WHERE clean_task_id = ?
                """, Integer.class, 92941L)).isEqualTo(0);
    }

    @Test
    @Timeout(60)
    void roomCategoryDelete_shouldAvoidUniqueConflictWhenSameRoomNameWasPreviouslySoftDeleted() throws Exception {
        insertRoomCategory(ROOM_CATEGORY_B, "TDD deleted same room name B", 1, 20);
        insertRoom(92961L, ROOM_CATEGORY_B, "DUP-101", 1);
        jdbcTemplate.update("""
                UPDATE room
                SET status = 0, is_deleted = 1
                WHERE room_id = ?
                """, 92961L);

        insertRoomCategory(ROOM_CATEGORY_A, "TDD delete same room name A", 1, 10);
        insertRoom(92962L, ROOM_CATEGORY_A, "DUP-101", 1);

        mockMvc.perform(post("/roomCategory/delete")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","roomCategoryId":"92901"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        assertThat(jdbcTemplate.queryForObject("""
                SELECT is_deleted
                FROM room
                WHERE room_id = ?
                """, Integer.class, 92962L)).isEqualTo(1);
    }

    private void insertRoomCategory(long roomCategoryId, String name, int roomCount, int sortNo) {
        jdbcTemplate.update("""
                        INSERT INTO room_category (
                            room_category_id,
                            camp_id,
                            poi_id,
                            group_id,
                            name,
                            display_name,
                            room_count,
                            status,
                            sort_no,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                roomCategoryId,
                CAMP_ID,
                POI_ID,
                GROUP_ID,
                name,
                name,
                roomCount,
                1,
                sortNo,
                0
        );
    }

    private void insertMedia(long mediaResourceId, String url) {
        jdbcTemplate.update("""
                        INSERT INTO media_resource (
                            media_resource_id,
                            camp_id,
                            path,
                            name,
                            is_dir,
                            url,
                            biz_type,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                mediaResourceId,
                CAMP_ID,
                "/test/" + mediaResourceId + ".png",
                "test-" + mediaResourceId + ".png",
                0,
                url,
                "room_category",
                0
        );
    }

    private void insertRoom(long roomId, long roomCategoryId, String roomName, int sortNo) {
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
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                roomId,
                CAMP_ID,
                POI_ID,
                roomCategoryId,
                roomName,
                roomName,
                1,
                sortNo,
                0
        );
    }

    private void insertLinkage(long id, long roomCategoryId, long linkedRoomCategoryId, int status) {
        jdbcTemplate.update("""
                        INSERT INTO room_category_linkage (
                            id,
                            camp_id,
                            room_category_id,
                            linked_room_category_id,
                            relation_type,
                            status
                        ) VALUES (?, ?, ?, ?, ?, ?)
                        """,
                id,
                CAMP_ID,
                roomCategoryId,
                linkedRoomCategoryId,
                "close_linkage",
                status
        );
    }

    private void insertCleanTask(long cleanTaskId, long roomId, long roomCategoryId) {
        jdbcTemplate.update("""
                        INSERT INTO clean_task (
                            clean_task_id,
                            camp_id,
                            poi_id,
                            room_id,
                            room_category_id,
                            task_type,
                            task_status
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                cleanTaskId,
                CAMP_ID,
                POI_ID,
                roomId,
                roomCategoryId,
                "checkout",
                "pending"
        );
    }
}
