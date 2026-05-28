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

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OtaQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CURRENT_USER_ID = 12001L;
    private static final long ISOLATED_CAMP_ID = 29931L;
    private static final long POI_ID = 29932L;
    private static final long CTRIP_ACCOUNT_ID = 29933L;
    private static final long BOOKING_ACCOUNT_ID = 29934L;
    private static final long LINKED_ROOM_CATEGORY_ID = 29935L;
    private static final long UNLINKED_ROOM_CATEGORY_ID = 29936L;
    private static final long CTRIP_POI_REL_ID = 29937L;
    private static final long CTRIP_ROOM_REL_ID = 29938L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void otaDashboardGet_shouldReturnChannelsAndMetricsFromRealChannelTables() throws Exception {
        seedOtaScene();

        mockMvc.perform(post("/ota/dashboard/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"29931",
                                  "businessDate":"2026-05-18",
                                  "storeId":"all",
                                  "dimension":"all"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.provider").value("api"))
                .andExpect(jsonPath("$.data.traceId").exists())
                .andExpect(jsonPath("$.data.request.businessDate").value("2026-05-18"))
                .andExpect(jsonPath("$.data.request.storeId").value("all"))
                .andExpect(jsonPath("$.data.request.dimension").value("all"))
                .andExpect(jsonPath("$.data.stores[0].value").value("all"))
                .andExpect(jsonPath("$.data.stores[0].label").value("全部门店"))
                .andExpect(jsonPath("$.data.stores[1].value").value("29932"))
                .andExpect(jsonPath("$.data.stores[1].label").value("OTA联调门店"))
                .andExpect(jsonPath("$.data.dimensions.length()").value(3))
                .andExpect(jsonPath("$.data.metrics[0].key").value("connected"))
                .andExpect(jsonPath("$.data.metrics[0].value").value("1"))
                .andExpect(jsonPath("$.data.metrics[1].key").value("pending"))
                .andExpect(jsonPath("$.data.metrics[1].value").value("1"))
                .andExpect(jsonPath("$.data.metrics[2].key").value("roomTypes"))
                .andExpect(jsonPath("$.data.metrics[2].value").value("1/2"))
                .andExpect(jsonPath("$.data.connectedChannels.length()").value(1))
                .andExpect(jsonPath("$.data.connectedChannels[0].id").value("ctrip"))
                .andExpect(jsonPath("$.data.connectedChannels[0].name").value("携程直连"))
                .andExpect(jsonPath("$.data.connectedChannels[0].relation").value("关联房型 1/2"))
                .andExpect(jsonPath("$.data.connectedChannels[0].roomTypeCount").value(2))
                .andExpect(jsonPath("$.data.connectedChannels[0].mappedRoomTypeCount").value(1))
                .andExpect(jsonPath("$.data.connectedChannels[0].lastSyncAt").value("2026-05-20 11:20"))
                .andExpect(jsonPath("$.data.connectedChannels[0].logoText").value("携程"))
                .andExpect(jsonPath("$.data.connectedChannels[0].detail", containsString("携程直连")))
                .andExpect(jsonPath("$.data.pendingChannels.length()").value(1))
                .andExpect(jsonPath("$.data.pendingChannels[0].id").value("booking"))
                .andExpect(jsonPath("$.data.pendingChannels[0].name").value("Booking"))
                .andExpect(jsonPath("$.data.pendingChannels[0].relation").value("等待授权"))
                .andExpect(jsonPath("$.data.quickLinks[0].route").value("/channels/ota/log"));
    }

    @Test
    @Timeout(60)
    void otaChannelDetailGet_shouldReturnStoreAndRoomRowsForSelectedChannel() throws Exception {
        seedOtaScene();

        mockMvc.perform(post("/ota/channel/detail/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"29931",
                                  "channelId":"ctrip"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("ctrip"))
                .andExpect(jsonPath("$.data.channelName").value("携程直连"))
                .andExpect(jsonPath("$.data.title").value("携程直连"))
                .andExpect(jsonPath("$.data.logoText").value("携程"))
                .andExpect(jsonPath("$.data.logoTone").value(1))
                .andExpect(jsonPath("$.data.noticeText", containsString("佣金率")))
                .andExpect(jsonPath("$.data.channelStoreOptions[0].value").value("all"))
                .andExpect(jsonPath("$.data.channelStoreOptions[1].value").value("29932"))
                .andExpect(jsonPath("$.data.channelStoreOptions[1].label").value("OTA联调门店"))
                .andExpect(jsonPath("$.data.accountOptions[0].value").value("all"))
                .andExpect(jsonPath("$.data.accountOptions[1].value").value("29933"))
                .andExpect(jsonPath("$.data.accountOptions[1].label").value("携程直连主账号"))
                .andExpect(jsonPath("$.data.statusOptions[1].value").value("linked"))
                .andExpect(jsonPath("$.data.roomRows.length()").value(2))
                .andExpect(jsonPath("$.data.roomRows[0].id").value("29938"))
                .andExpect(jsonPath("$.data.roomRows[0].channelStoreId").value("29932"))
                .andExpect(jsonPath("$.data.roomRows[0].channelStoreName").value("OTA联调门店"))
                .andExpect(jsonPath("$.data.roomRows[0].channelRoomType").value("CTRIP-DELUXE-ROOM"))
                .andExpect(jsonPath("$.data.roomRows[0].status").value("linked"))
                .andExpect(jsonPath("$.data.roomRows[0].statusLabel").value("已关联"))
                .andExpect(jsonPath("$.data.roomRows[0].linkedRoomType").value("OTA联调豪华房"))
                .andExpect(jsonPath("$.data.roomRows[1].status").value("unlinked"))
                .andExpect(jsonPath("$.data.roomRows[1].statusLabel").value("未关联"))
                .andExpect(jsonPath("$.data.roomRows[1].linkedRoomType").value("-"))
                .andExpect(jsonPath("$.data.storeRows.length()").value(1))
                .andExpect(jsonPath("$.data.storeRows[0].id").value("29937"))
                .andExpect(jsonPath("$.data.storeRows[0].accountId").value("29933"))
                .andExpect(jsonPath("$.data.storeRows[0].channelStoreId").value("29932"))
                .andExpect(jsonPath("$.data.storeRows[0].channelStoreName").value("OTA联调门店"))
                .andExpect(jsonPath("$.data.storeRows[0].hotelType").value("预付"))
                .andExpect(jsonPath("$.data.storeRows[0].hotelId").value("CTRIP-POI-001"))
                .andExpect(jsonPath("$.data.storeRows[0].relatedRoomTypeSummary").value("1/2"))
                .andExpect(jsonPath("$.data.storeRows[0].status").value("linked"))
                .andExpect(jsonPath("$.data.syncStoreDefaults.hotelSubtype").value("prepay"));
    }

    private void seedOtaScene() {
        insertCamp();
        rebindCurrentUserCamp();
        insertPoi();
        insertRoomCategory(LINKED_ROOM_CATEGORY_ID, "OTA联调豪华房", 1);
        insertRoomCategory(UNLINKED_ROOM_CATEGORY_ID, "OTA联调未映射房", 2);
        insertChannelAccount(CTRIP_ACCOUNT_ID, 5L, "携程直连", "携程直连主账号", "authorized",
                LocalDateTime.of(2026, 5, 18, 9, 0, 0),
                LocalDateTime.of(2026, 5, 20, 11, 10, 0));
        insertChannelAccount(BOOKING_ACCOUNT_ID, 22L, "Booking", "Booking待授权账号", "pending",
                null,
                LocalDateTime.of(2026, 5, 20, 10, 0, 0));
        insertChannelPoiRel();
        insertChannelRoomCategoryRel();
    }

    private void rebindCurrentUserCamp() {
        jdbcTemplate.update("UPDATE pms_member SET camp_id = ? WHERE user_id = ?", ISOLATED_CAMP_ID, CURRENT_USER_ID);
    }

    private void insertCamp() {
        jdbcTemplate.update("""
                        INSERT INTO pms_camp (
                            camp_id,
                            name,
                            type,
                            city_name,
                            address,
                            contact_number,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                ISOLATED_CAMP_ID,
                "OTA联调租户",
                1,
                "深圳",
                "南山区OTA联调路 31 号",
                "0755-2993101",
                1,
                0
        );
    }

    private void insertPoi() {
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
                POI_ID,
                ISOLATED_CAMP_ID,
                "OTA联调门店",
                1,
                1,
                1,
                0
        );
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
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                roomCategoryId,
                ISOLATED_CAMP_ID,
                POI_ID,
                name,
                name,
                1,
                2,
                32000L,
                35000L,
                38000L,
                14,
                23,
                12,
                "OTA房型亮点",
                "OTA房型周边",
                "ota query room category",
                sortNo,
                1,
                0
        );
    }

    private void insertChannelAccount(
            long accountId,
            long channelId,
            String channelName,
            String accountName,
            String status,
            LocalDateTime authorizedAt,
            LocalDateTime updatedAt
    ) {
        jdbcTemplate.update("""
                        INSERT INTO channel_account (
                            account_id,
                            camp_id,
                            channel_id,
                            channel_name,
                            account_name,
                            out_account_id,
                            status,
                            authorized_at,
                            updated_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                accountId,
                ISOLATED_CAMP_ID,
                channelId,
                channelName,
                accountName,
                "OUT-" + accountId,
                status,
                authorizedAt == null ? null : Timestamp.valueOf(authorizedAt),
                Timestamp.valueOf(updatedAt)
        );
    }

    private void insertChannelPoiRel() {
        jdbcTemplate.update("""
                        INSERT INTO channel_poi_rel (
                            id,
                            camp_id,
                            account_id,
                            poi_id,
                            out_poi_id,
                            sync_status,
                            created_at,
                            updated_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                CTRIP_POI_REL_ID,
                ISOLATED_CAMP_ID,
                CTRIP_ACCOUNT_ID,
                POI_ID,
                "CTRIP-POI-001",
                "success",
                Timestamp.valueOf(LocalDateTime.of(2026, 5, 20, 11, 0, 0)),
                Timestamp.valueOf(LocalDateTime.of(2026, 5, 20, 11, 15, 0))
        );
    }

    private void insertChannelRoomCategoryRel() {
        jdbcTemplate.update("""
                        INSERT INTO channel_room_category_rel (
                            id,
                            camp_id,
                            account_id,
                            room_category_id,
                            out_room_category_id,
                            project_type,
                            shelf_status,
                            audit_status,
                            created_at,
                            updated_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                CTRIP_ROOM_REL_ID,
                ISOLATED_CAMP_ID,
                CTRIP_ACCOUNT_ID,
                LINKED_ROOM_CATEGORY_ID,
                "CTRIP-DELUXE-ROOM",
                "calendar_room",
                "on_shelf",
                "approved",
                Timestamp.valueOf(LocalDateTime.of(2026, 5, 20, 11, 10, 0)),
                Timestamp.valueOf(LocalDateTime.of(2026, 5, 20, 11, 20, 0))
        );
    }
}
