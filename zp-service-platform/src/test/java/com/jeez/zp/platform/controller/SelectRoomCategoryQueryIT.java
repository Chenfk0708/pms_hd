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
class SelectRoomCategoryQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String CAMP_ID = "10001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void selectRoomCategoryPageGet_shouldReturnPagedRowsWhenChannelIdIsZero() throws Exception {
        insertPoi(99111L, "TDD鎴垮瀷涓嬫媺闂ㄥ簵A", 10);
        insertRoomCategory(92201L, 99111L, "TDD涓嬫媺鎴垮瀷A", 1, 10);
        insertRoomCategory(92202L, 99111L, "TDD涓嬫媺鎴垮瀷B", 1, 20);
        insertRoomCategory(92203L, 99111L, "TDD涓嬫媺鍋滅敤鎴垮瀷", 0, 30);

        mockMvc.perform(post("/select/roomCategory/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageSize":999,
                                  "current":1,
                                  "poiId":"99111",
                                  "channelId":0,
                                  "isAvailability":"1"
                                }
                                """))
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
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value("92201"))
                .andExpect(jsonPath("$.data.list[0].id").value("92201"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("TDD涓嬫媺鎴垮瀷A"))
                .andExpect(jsonPath("$.data.list[0].name").value("TDD涓嬫媺鎴垮瀷A灞曠ず"))
                .andExpect(jsonPath("$.data.list[0].poiId").value("99111"))
                .andExpect(jsonPath("$.data.list[0].poiName").value("TDD鎴垮瀷涓嬫媺闂ㄥ簵A"))
                .andExpect(jsonPath("$.data.list[1].roomCategoryId").value("92202"));
    }

    @Test
    @Timeout(60)
    void selectRoomCategoryPageGet_shouldFallbackCurrentCampWhenCampIdBlank() throws Exception {
        insertPoi(99112L, "TDD鎴垮瀷涓嬫媺闂ㄥ簵B", 20);
        insertRoomCategory(92301L, 99112L, "TDD鍥炶惤鎴垮瀷", 1, 10);

        mockMvc.perform(post("/select/roomCategory/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "pageSize":999,
                                  "pageNum":1,
                                  "poiId":"99112",
                                  "channelId":0,
                                  "isAvailability":"1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value("92301"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("TDD鍥炶惤鎴垮瀷"));
    }

    @Test
    @Timeout(60)
    void selectRoomCategoryPageGet_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/select/roomCategory/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "pageSize":999,
                                  "pageNum":1,
                                  "channelId":0,
                                  "isAvailability":"1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @Timeout(60)
    void selectRoomCategoryPageGet_shouldFilterByChannelId() throws Exception {
        insertPoi(99113L, "TDD鎴垮瀷涓嬫媺闂ㄥ簵C", 30);
        insertRoomCategory(92401L, 99113L, "TDD娓犻亾鎴垮瀷A", 1, 10);
        insertRoomCategory(92402L, 99113L, "TDD娓犻亾鎴垮瀷B", 1, 20);
        insertChannelAccount(99501L, 91L, "TDD娓犻亾91", "TDD璐﹀彿91", "OUT-ACC-91");
        insertChannelRelation(99601L, 99501L, 92401L, "OUT-RC-91");

        mockMvc.perform(post("/select/roomCategory/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageSize":999,
                                  "pageNum":1,
                                  "poiId":"99113",
                                  "channelId":91,
                                  "isAvailability":"1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value("92401"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("TDD娓犻亾鎴垮瀷A"));
    }

    @Test
    @Timeout(60)
    void selectRoomCategoryPageGet_shouldUseFilterSyncChannelIdWhenChannelIdIsZero() throws Exception {
        insertPoi(99114L, "TDD鎴垮瀷涓嬫媺闂ㄥ簵D", 40);
        insertRoomCategory(92501L, 99114L, "TDD鍚屾娓犻亾鎴垮瀷A", 1, 10);
        insertRoomCategory(92502L, 99114L, "TDD鍚屾娓犻亾鎴垮瀷B", 1, 20);
        insertChannelAccount(99502L, 92L, "TDD娓犻亾92", "TDD璐﹀彿92", "OUT-ACC-92");
        insertChannelRelation(99602L, 99502L, 92501L, "OUT-RC-92");

        mockMvc.perform(post("/select/roomCategory/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageSize":999,
                                  "pageNum":1,
                                  "poiId":"99114",
                                  "channelId":0,
                                  "filterSyncChannelId":92,
                                  "isFilterAlreadyFlow":1,
                                  "isAvailability":"1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value("92501"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("TDD鍚屾娓犻亾鎴垮瀷A"));
    }

    private void insertPoi(long poiId, String poiName, int sortNo) {
        jdbcTemplate.update("""
                        INSERT INTO pms_poi (
                            poi_id,
                            camp_id,
                            poi_name,
                            is_availability,
                            sort_no,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                poiId,
                10001L,
                poiName,
                1,
                sortNo,
                1,
                0
        );
    }

    private void insertRoomCategory(long roomCategoryId, long poiId, String name, int status, int sortNo) {
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
                poiId,
                null,
                name,
                name + "灞曠ず",
                1,
                2,
                26800L,
                28800L,
                30800L,
                14,
                23,
                12,
                name + "浜偣",
                name + "鍛ㄨ竟",
                name + "鍥炬枃",
                sortNo,
                status,
                0
        );
    }

    private void insertChannelAccount(long accountId, long channelId, String channelName, String accountName, String outAccountId) {
        jdbcTemplate.update("""
                        INSERT INTO channel_account (
                            account_id,
                            camp_id,
                            channel_id,
                            channel_name,
                            account_name,
                            out_account_id,
                            status
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                accountId,
                10001L,
                channelId,
                channelName,
                accountName,
                outAccountId,
                "authorized"
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
