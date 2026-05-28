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
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PoiQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void poiPageGet_shouldReturnPagedPoiListForCurrentCamp() throws Exception {
        mockMvc.perform(post("/select/poi/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageSize":999,
                                  "pageNum":1,
                                  "channelId":0,
                                  "isAvailability":"1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.size").value(999))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.hasNextPage").value(false))
                .andExpect(jsonPath("$.data.pages").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].poiId").value("11001"))
                .andExpect(jsonPath("$.data.list[0].poiName").value("路客云演示门店"));
    }

    @Test
    @Timeout(60)
    void poiPageGet_shouldFallbackCurrentCampWhenCampIdBlank() throws Exception {
        mockMvc.perform(post("/select/poi/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "pageSize":999,
                                  "pageNum":1,
                                  "channelId":0,
                                  "isAvailability":"1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].poiId").value("11001"))
                .andExpect(jsonPath("$.data.list[0].poiName").value("路客云演示门店"));
    }

    @Test
    @Timeout(60)
    void poiPageGet_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/select/poi/page/get")
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
    void poiPageGet_shouldFilterByChannelId() throws Exception {
        insertPoi(99101L, "TDD渠道门店A", 1, 10);
        insertPoi(99102L, "TDD渠道门店B", 1, 20);
        insertChannelAccount(99401L, 91L, "TDD渠道91", "TDD账号91", "OUT-ACC-91");
        insertChannelAccount(99402L, 92L, "TDD渠道92", "TDD账号92", "OUT-ACC-92");
        insertChannelPoiRelation(99201L, 99401L, 99101L, "TDD-POI-A");
        insertChannelPoiRelation(99202L, 99402L, 99102L, "TDD-POI-B");

        mockMvc.perform(post("/select/poi/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageSize":999,
                                  "pageNum":1,
                                  "channelId":91,
                                  "isAvailability":"1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].poiId").value("99101"))
                .andExpect(jsonPath("$.data.list[0].poiName").value("TDD渠道门店A"));
    }

    @Test
    @Timeout(60)
    void poiPageGet_shouldFilterByAvailabilityWhenRequested() throws Exception {
        insertPoi(99301L, "TDD可售门店", 1, 30);
        insertPoi(99302L, "TDD停售门店", 0, 31);

        mockMvc.perform(post("/select/poi/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageSize":999,
                                  "pageNum":1,
                                  "channelId":0,
                                  "isAvailability":"1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list[*].poiId", hasItem("99301")))
                .andExpect(jsonPath("$.data.list[*].poiId", not(hasItem("99302"))));
    }

    private void insertPoi(long poiId, String poiName, int isAvailability, int sortNo) {
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
                isAvailability,
                sortNo,
                1,
                0
        );
    }

    private void insertChannelPoiRelation(long id, long accountId, long poiId, String outPoiId) {
        jdbcTemplate.update("""
                        INSERT INTO channel_poi_rel (
                            id,
                            camp_id,
                            account_id,
                            poi_id,
                            out_poi_id,
                            sync_status
                        ) VALUES (?, ?, ?, ?, ?, ?)
                        """,
                id,
                10001L,
                accountId,
                poiId,
                outPoiId,
                "synced"
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
}
