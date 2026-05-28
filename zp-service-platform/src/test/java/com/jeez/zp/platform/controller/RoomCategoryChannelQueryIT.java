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
class RoomCategoryChannelQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void calChannel4RoomCategoryGet_shouldReturnAuthorizedChannelsWithoutSelfAndWithDualShapes() throws Exception {
        resetChannelAccounts();
        insertChannelAccount(26101L, 17L, "路客云聚合", "tdd-localhome", "OUT-17-A", "authorized");
        insertChannelAccount(26102L, 5L, "携程", "tdd-ctrip", "OUT-5-A", "authorized");
        insertChannelAccount(26103L, 17L, "路客云聚合", "tdd-localhome-dup", "OUT-17-B", "authorized");

        mockMvc.perform(post("/select/calChannel4RoomCategory/get")
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
                .andExpect(jsonPath("$.data.select.length()").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.select[0].channelId").value("17"))
                .andExpect(jsonPath("$.data.select[0].channelName").value("路客云聚合"))
                .andExpect(jsonPath("$.data.select[0].id").value("17"))
                .andExpect(jsonPath("$.data.select[0].name").value("路客云聚合"))
                .andExpect(jsonPath("$.data.select[1].channelId").value("5"))
                .andExpect(jsonPath("$.data.list[1].channelName").value("携程"));
    }

    @Test
    @Timeout(60)
    void calChannel4RoomCategoryGet_shouldFallbackCurrentCampWhenCampIdBlank() throws Exception {
        resetChannelAccounts();
        insertChannelAccount(26201L, 17L, "路客云聚合", "tdd-localhome", "OUT-17-A", "authorized");

        mockMvc.perform(post("/select/calChannel4RoomCategory/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":""}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.select.length()").value(1))
                .andExpect(jsonPath("$.data.select[0].channelId").value("17"));
    }

    @Test
    @Timeout(60)
    void calChannel4RoomCategoryGet_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/select/calChannel4RoomCategory/get")
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

    @Test
    @Timeout(60)
    void calChannel4RoomCategoryGet_shouldExcludeNonAuthorizedChannels() throws Exception {
        resetChannelAccounts();
        insertChannelAccount(26301L, 17L, "路客云聚合", "tdd-localhome", "OUT-17-A", "authorized");
        insertChannelAccount(26302L, 8L, "飞猪淘酒店", "tdd-fliggy", "OUT-8-A", "expired");

        mockMvc.perform(post("/select/calChannel4RoomCategory/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.select.length()").value(1))
                .andExpect(jsonPath("$.data.select[0].channelId").value("17"));
    }

    private void resetChannelAccounts() {
        jdbcTemplate.update("DELETE FROM channel_room_category_rel WHERE camp_id = ?", 10001L);
        jdbcTemplate.update("DELETE FROM channel_poi_rel WHERE camp_id = ?", 10001L);
        jdbcTemplate.update("DELETE FROM channel_account WHERE camp_id = ?", 10001L);
    }

    private void insertChannelAccount(
            long accountId,
            long channelId,
            String channelName,
            String accountName,
            String outAccountId,
            String status
    ) {
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
                status
        );
    }
}
