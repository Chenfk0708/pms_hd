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
class RoomCategoryQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String CAMP_ID = "10001";
    private static final long POI_ID = 11001L;
    private static final long GROUP_ID = 21001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void roomCategoriesPageGet_shouldReturnPagedRowsWithRoomsPoiAndProductInfo() throws Exception {
        insertRoomCategory(92001L, "TDD房型A", 2, 26800L, 28800L, 30800L, 14, 23, 12, 10);
        insertRoomCategory(92002L, "TDD房型B", 1, 36800L, 38800L, 40800L, 15, 24, 13, 20);
        insertRoom(93001L, 92001L, "A-101");
        insertRoom(93002L, 92001L, "A-102");
        insertRoom(93003L, 92002L, "B-201");
        insertMedia(94001L, "https://example.com/tdd-room-a.jpg");
        insertMedia(94002L, "https://example.com/tdd-room-b.jpg");
        insertRoomCategoryMedia(95001L, 92001L, 94001L, "https://example.com/tdd-room-a.jpg");
        insertRoomCategoryMedia(95002L, 92002L, 94002L, "https://example.com/tdd-room-b.jpg");

        mockMvc.perform(post("/roomCategories/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"%s",
                                  "pageSize":999,
                                  "pageNum":1,
                                  "roomCategoryName":"TDD房型",
                                  "keyword":"",
                                  "cityIds":[],
                                  "channelId":""
                                }
                                """.formatted(CAMP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.size").value(999))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.hasNextPage").value(false))
                .andExpect(jsonPath("$.data.pages").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value("92001"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("TDD房型A"))
                .andExpect(jsonPath("$.data.list[0].name").value("TDD房型A展示"))
                .andExpect(jsonPath("$.data.list[0].poiId").value("11001"))
                .andExpect(jsonPath("$.data.list[0].poiName").value("路客云演示门店"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryGroupId").value("21001"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryGroupName").value("标准客房"))
                .andExpect(jsonPath("$.data.list[0].roomNum").value(2))
                .andExpect(jsonPath("$.data.list[0].roomNames").value("A-101,A-102"))
                .andExpect(jsonPath("$.data.list[0].mainPhoto").value("https://example.com/tdd-room-a.jpg"))
                .andExpect(jsonPath("$.data.list[0].poiView.poiId").value("11001"))
                .andExpect(jsonPath("$.data.list[0].poiView.name").value("路客云演示门店"))
                .andExpect(jsonPath("$.data.list[0].roomViews.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].roomViews[0].roomName").value("A-101"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryProductInfoViews[0].normalPrice").value(26800))
                .andExpect(jsonPath("$.data.list[0].roomCategoryProductInfoViews[0].earliestCheckInTime").value(14))
                .andExpect(jsonPath("$.data.list[1].roomCategoryId").value("92002"));
    }

    @Test
    @Timeout(60)
    void roomCategoriesPageGet_shouldFilterByChannelId() throws Exception {
        insertRoomCategory(92101L, "渠道筛选房型A", 1, 18800L, 20800L, 22800L, 14, 23, 12, 10);
        insertRoomCategory(92102L, "渠道筛选房型B", 1, 19800L, 21800L, 23800L, 14, 23, 12, 20);
        insertChannelRelation(96101L, 25301L, 92101L, "MT-TEST-001");
        insertChannelRelation(96102L, 25302L, 92102L, "CTRIP-TEST-002");

        mockMvc.perform(post("/roomCategories/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"%s",
                                  "pageSize":999,
                                  "pageNum":1,
                                  "roomCategoryName":"渠道筛选房型",
                                  "keyword":"",
                                  "cityIds":[],
                                  "channelId":"1"
                                }
                                """.formatted(CAMP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value("92101"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("渠道筛选房型A"));
    }

    @Test
    @Timeout(60)
    void roomCategoriesPageGet_shouldFallbackCurrentCampAndReturnEmptyPage() throws Exception {
        mockMvc.perform(post("/roomCategories/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "pageSize":999,
                                  "pageNum":1,
                                  "roomCategoryName":"不存在的房型",
                                  "keyword":"",
                                  "cityIds":[],
                                  "channelId":""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.size").value(999))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.hasNextPage").value(false))
                .andExpect(jsonPath("$.data.pages").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(0));
    }

    @Test
    @Timeout(60)
    void roomCategoriesPageGet_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/roomCategories/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "pageSize":999,
                                  "pageNum":1,
                                  "roomCategoryName":"",
                                  "keyword":"",
                                  "cityIds":[],
                                  "channelId":""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private void insertRoomCategory(
            long roomCategoryId,
            String name,
            int roomCount,
            long weekdayPriceCent,
            long weekendPriceCent,
            long holidayPriceCent,
            int earliestCheckInHour,
            int latestCheckInHour,
            int latestCheckOutHour,
            int sortNo
    ) {
        jdbcTemplate.update("""
                        INSERT INTO room_category (
                            room_category_id,
                            camp_id,
                            poi_id,
                            group_id,
                            name,
                            display_name,
                            room_count,
                            guest_count,
                            weekday_price_cent,
                            weekend_price_cent,
                            holiday_price_cent,
                            earliest_check_in_hour,
                            latest_check_in_hour,
                            latest_check_out_hour,
                            highlight_description,
                            nearby_description,
                            article_description,
                            sort_no,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                roomCategoryId,
                10001L,
                POI_ID,
                GROUP_ID,
                name,
                name + "展示",
                roomCount,
                2,
                weekdayPriceCent,
                weekendPriceCent,
                holidayPriceCent,
                earliestCheckInHour,
                latestCheckInHour,
                latestCheckOutHour,
                name + "亮点",
                name + "周边",
                name + "图文",
                sortNo,
                1,
                0
        );
    }

    private void insertRoom(long roomId, long roomCategoryId, String roomName) {
        jdbcTemplate.update("""
                        INSERT INTO room (
                            room_id,
                            camp_id,
                            poi_id,
                            room_category_id,
                            room_name,
                            lock_status,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                roomId,
                10001L,
                POI_ID,
                roomCategoryId,
                roomName,
                "online",
                1,
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
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                mediaResourceId,
                10001L,
                "/test/" + mediaResourceId + ".jpg",
                "test-" + mediaResourceId + ".jpg",
                0,
                url,
                0
        );
    }

    private void insertRoomCategoryMedia(long mediaId, long roomCategoryId, long mediaResourceId, String mediaUrl) {
        jdbcTemplate.update("""
                        INSERT INTO room_category_media (
                            media_id,
                            camp_id,
                            room_category_id,
                            media_resource_id,
                            media_url,
                            media_type,
                            scene_type,
                            sort_no,
                            status
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                mediaId,
                10001L,
                roomCategoryId,
                mediaResourceId,
                mediaUrl,
                "image",
                "cover",
                1,
                1
        );
    }

    private void insertChannelRelation(long id, long accountId, long roomCategoryId, String outRoomCategoryId) {
        jdbcTemplate.update("""
                        INSERT INTO channel_room_category_rel (
                            id,
                            camp_id,
                            account_id,
                            room_category_id,
                            out_room_category_id,
                            project_type,
                            shelf_status,
                            audit_status
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                id,
                10001L,
                accountId,
                roomCategoryId,
                outRoomCategoryId,
                "calendar_room",
                "on_shelf",
                "approved"
        );
    }
}
